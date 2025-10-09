package com.example.warhammer40kscanner

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.warhammer40kscanner.databinding.ActivityLoreBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoreBinding

    private var idCapituloSeleccionado: Long = -1
    var mainModels: MutableList<MainModel> = mutableListOf()
    lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.subfaccionesRV.layoutManager = layoutManager
        binding.subfaccionesRV.itemAnimator = DefaultItemAnimator()

        binding.btnVolver.setOnClickListener {
            // Si estamos viendo la lista de modelos/unidades/mejoras (detalles)
            if (binding.detalleRecyclerView.visibility == View.VISIBLE) {
                binding.detalleRecyclerView.visibility = View.GONE
                binding.botonesLayout.visibility = View.GONE
                binding.subfaccionesRV.visibility = View.VISIBLE
                binding.sesiontv2.text = getString(R.string.subfactions_lore)
                cargarCapitulos(idCapituloSeleccionado) // Volver a capítulos
            } else {
                // Si estamos en capítulos y pulsamos volver, ir a subfacciones
                binding.subfaccionesRV.visibility = View.VISIBLE
                binding.btnVolver.visibility = View.GONE
                binding.sesiontv2.text = getString(R.string.subfactions_lore)
                cargarSubfacciones()
            }
        }

        cargarSubfacciones()
    }

    private fun cargarSubfacciones() {
        val imageList = listOf(
            R.drawable.black_templar,
            R.drawable.chaos_spacemarines,
            R.drawable.aeldari_artwork
        )

        ApiClient.apiService.getSubfacciones().enqueue(object : Callback<List<Subfaccion>> {
            override fun onResponse(call: Call<List<Subfaccion>>, response: Response<List<Subfaccion>>) {
                if (response.isSuccessful) {
                    val subfactions = response.body() ?: emptyList()
                    val mainModels = mutableListOf<MainModel>()

                    for (i in subfactions.indices) {
                        val nombre = subfactions[i].nombre
                        val imageRes = if (i < imageList.size) imageList[i] else R.drawable.ultra_vs_deathguard
                        mainModels.add(MainModel(imageRes, nombre))
                    }

                    mainAdapter = MainAdapter(this@LoreActivity, ArrayList(mainModels)) { index ->
                        val selectedSubfaction = subfactions[index]
                        val idSubfaction = selectedSubfaction.id
                        cargarCapitulos(idSubfaction)
                    }

                    binding.subfaccionesRV.adapter = mainAdapter
                } else {
                    Log.e("API", "Respuesta no exitosa: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Subfaccion>>, t: Throwable) {
                Log.e("API", "Error en llamada: ${t.message}")
            }
        })
    }

    private fun cargarCapitulos(idSubfaccion: Long) {
        ApiClient.apiService.getCapitulos(idSubfaccion).enqueue(object : Callback<List<Capitulo>> {
            override fun onResponse(call: Call<List<Capitulo>>, response: Response<List<Capitulo>>) {
                if (response.isSuccessful) {
                    val capitulos = response.body() ?: emptyList()
                    val chapterImageList = getChapterImageList(idSubfaccion)

                    val chapterModels = capitulos.mapIndexed { index, capitulo ->
                        val imageRes = if (index < chapterImageList.size) chapterImageList[index] else R.drawable.ultra_vs_deathguard
                        MainModel(imageRes, capitulo.name)
                    }

                    mainAdapter = MainAdapter(this@LoreActivity, ArrayList(chapterModels)) { index ->
                        val capitulo = capitulos[index]
                        idCapituloSeleccionado = capitulo.id  // <-- Guardamos el id
                        mostrarVistaCapitulo(capitulo.name)
                    }

                    binding.subfaccionesRV.adapter = mainAdapter
                }
                binding.btnVolver.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<List<Capitulo>>, t: Throwable) {
                Log.e("API", "Error cargando capítulos: ${t.message}")
            }
        })
    }

    private fun mostrarVistaCapitulo(nombreCapitulo: String) {
        //binding.subfaccionesRV.adapter = null
        binding.subfaccionesRV.visibility = View.GONE
        binding.sesiontv2.text = nombreCapitulo

        binding.botonesLayout.visibility = View.VISIBLE
        binding.detalleRecyclerView.visibility = View.VISIBLE
        binding.btnVolver.visibility = View.VISIBLE

        val verticalLayoutManager = LinearLayoutManager(this)
        binding.detalleRecyclerView.layoutManager = verticalLayoutManager
        binding.detalleRecyclerView.itemAnimator = DefaultItemAnimator()

        binding.btnModelos.setOnClickListener { cargarDatosTipo("model") }
        binding.btnUnidades.setOnClickListener { cargarDatosTipo("unit") }
        binding.btnMejoras.setOnClickListener { cargarDatosTipo("upgrade") }

        cargarDatosTipo("model")
    }

    private var selectionEntries = mutableListOf<SelectionEntry>()

    private fun cargarDatosTipo(tipo: String) {
        val call: Call<List<SelectionEntry>> = when (tipo) {
            "model" -> ApiClient.apiService.getModels(idCapituloSeleccionado)
            "unit" -> ApiClient.apiService.getUnits(idCapituloSeleccionado)
            "upgrade" -> ApiClient.apiService.getUpgrades(idCapituloSeleccionado)
            else -> return
        }

        call.enqueue(object : Callback<List<SelectionEntry>> {
            override fun onResponse(
                call: Call<List<SelectionEntry>>,
                response: Response<List<SelectionEntry>>
            ) {
                if (response.isSuccessful) {
                    selectionEntries = response.body()?.toMutableList() ?: mutableListOf()
                    Log.d("PUNTOS", "Recibidos desde API ($tipo): ${selectionEntries.map { it.name to it.puntos }}")
                    binding.detalleRecyclerView.adapter = SelectionAdapter(selectionEntries) { seleccion, position ->
                        cargarDescripcionSeleccionExpandida(seleccion.id, position)
                    }
                }
            }

            override fun onFailure(call: Call<List<SelectionEntry>>, t: Throwable) {
                Log.e("API", "Error al cargar $tipo: ${t.message}")
            }
        })
    }

    private fun cargarDescripcionSeleccionExpandida(idSelection: Long, position: Int) {
        val seleccion = selectionEntries[position]

        // 1. Descripción
        ApiClient.apiService.getDescriptionsBySelection(idSelection).enqueue(object : Callback<List<InfoLink>> {
            override fun onResponse(call: Call<List<InfoLink>>, response: Response<List<InfoLink>>) {
                if (response.isSuccessful) {
                    val descripciones = response.body() ?: emptyList()
                    if (descripciones.isNotEmpty()) {
                        selectionEntries[position] = selectionEntries[position].copy(
                            description = descripciones[0].description,
                            imageUrl = descripciones[0].imageUrl
                        )
                        binding.detalleRecyclerView.adapter?.notifyItemChanged(position)
                    }
                }
            }

            override fun onFailure(call: Call<List<InfoLink>>, t: Throwable) {
                Log.e("API", "Error cargando descripción: ${t.message}")
            }
        })

        // 2. Estadísticas
        ApiClient.apiService.getEstadisticasUnidad(seleccion.name).enqueue(object : Callback<StatisticsDTO> {
            override fun onResponse(call: Call<StatisticsDTO>, response: Response<StatisticsDTO>) {
                if (response.isSuccessful) {
                    val estadisticas = response.body()
                    selectionEntries[position] =
                        selectionEntries[position].copy(statistics = estadisticas)
                    binding.detalleRecyclerView.adapter?.notifyItemChanged(position)
                }
            }

            override fun onFailure(call: Call<StatisticsDTO>, t: Throwable) {
                Log.e("API", "Error cargando estadísticas: ${t.message}")
            }
        })

        // 3. Coste en puntos
        ApiClient.apiService.getCostBySelection(seleccion.id).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                val puntosRecibidos = response.body()
                Log.d("PUNTOS_API", "Puntos recibidos para ${seleccion.name}: $puntosRecibidos")

                if (response.isSuccessful) {
                    selectionEntries[position] =
                        selectionEntries[position].copy(puntos = puntosRecibidos)
                    binding.detalleRecyclerView.adapter?.notifyItemChanged(position)
                } else {
                    Log.e("PUNTOS_API", "Respuesta no exitosa para ${seleccion.name}: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("PUNTOS_API", "Error al obtener puntos de ${seleccion.name}: ${t.message}")
            }
        })
    }



    private fun mostrarDialogoDescripcion(texto: String) {
        AlertDialog.Builder(this)
            .setTitle("Descripción")
            .setMessage(texto)
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getChapterImageList(idSubfaccion: Long): List<Int> {
        return when (idSubfaccion.toInt()) {
            1 -> listOf(
                R.drawable.darkangels,
                R.drawable.ultramarines,
                R.drawable.bloodangels,
                R.drawable.ironhands,
                R.drawable.ravenguard
            )
            2 -> listOf(
                R.drawable.worldeaters,
                R.drawable.blacklegion,
                R.drawable.deathguard,
                R.drawable.emperorschildren,
                R.drawable.ironwarriors
            )
            3 -> listOf(
                R.drawable.drukhari,
                R.drawable.asuryani,
                R.drawable.harlequins,
                R.drawable.ynnari
            )
            else -> emptyList()
        }
    }
}
