package com.example.warhammer40kscanner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectionAdapter(
    private val items: List<SelectionEntry>,
    private val itemClickListener: (SelectionEntry, Int) -> Unit
) : RecyclerView.Adapter<SelectionAdapter.SelectionViewHolder>() {

    private var expandedPosition = -1


    inner class SelectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.nombreItem)
        val puntos: TextView = view.findViewById(R.id.puntosItem)
        val descripcion: TextView = view.findViewById(R.id.descripcionItem)
        val imageItem: ImageView = view.findViewById(R.id.imageItem)
        val statsTable: TableLayout = view.findViewById(R.id.statsTable)
        val statM: TextView = view.findViewById(R.id.statM)
        val statT: TextView = view.findViewById(R.id.statT)
        val statW: TextView = view.findViewById(R.id.statW)
        val statLD: TextView = view.findViewById(R.id.statLD)
        val statSV: TextView = view.findViewById(R.id.statSV)
        val statOC: TextView = view.findViewById(R.id.statOC)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selection, parent, false)
        return SelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
        val item = items[position]

        Log.d("PUNTOS", "Bind de ${item.name} -> puntos: ${item.puntos}")

        holder.nombre.text = item.name
        //holder.puntos.text = item.puntos?.let { "$it pts" } ?: "..."
        holder.puntos.text = if (item.puntos != null) "${item.puntos} pts" else "..."


        holder.descripcion.text = item.description ?: ""
        holder.descripcion.visibility = if (position == expandedPosition) View.VISIBLE else View.GONE

        // Imagen
        if (position == expandedPosition && !item.imageUrl.isNullOrEmpty()) {
            val resId = holder.imageItem.context.resources.getIdentifier(
                item.imageUrl, "drawable", holder.imageItem.context.packageName
            )
            if (resId != 0) {
                holder.imageItem.setImageResource(resId)
                holder.imageItem.visibility = View.VISIBLE
            } else {
                holder.imageItem.visibility = View.GONE
            }
        } else {
            holder.imageItem.visibility = View.GONE
        }

        // Tabla de estadísticas
        if (position == expandedPosition && item.statistics != null) {
            holder.statM.text = item.statistics?.M ?: "-"
            holder.statT.text = item.statistics?.T ?: "-"
            holder.statW.text = item.statistics?.W ?: "-"
            holder.statLD.text = item.statistics?.LD ?: "-"
            holder.statSV.text = item.statistics?.SV ?: "-"
            holder.statOC.text = item.statistics?.OC ?: "-"
            holder.statsTable.visibility = View.VISIBLE
        } else {
            holder.statsTable.visibility = View.GONE
        }

        // Expandir al hacer clic
        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousExpandedPosition = expandedPosition
            expandedPosition = if (adapterPosition == expandedPosition) -1 else adapterPosition

            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(expandedPosition)

            itemClickListener(items[adapterPosition], adapterPosition)
        }

        // Imagen ampliada con fade
        holder.imageItem.setOnClickListener {
            val context = holder.imageItem.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fullscreen_image, null)
            val imageView = dialogView.findViewById<ImageView>(R.id.fullscreenImage)

            val resId = context.resources.getIdentifier(
                item.imageUrl, "drawable", context.packageName
            )
            if (resId != 0) imageView.setImageResource(resId)

            val dialog = android.app.AlertDialog.Builder(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setView(dialogView).create()

            dialog.setOnShowListener {
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                dialogView.startAnimation(fadeIn)
            }

            dialogView.setOnClickListener {
                val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                dialogView.startAnimation(fadeOut)
                dialogView.postDelayed({ dialog.dismiss() }, 300)
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int = items.size
}