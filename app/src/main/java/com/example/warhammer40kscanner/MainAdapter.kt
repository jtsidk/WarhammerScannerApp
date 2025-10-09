package com.example.warhammer40kscanner


//import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView

class MainAdapter(private val context: android.content.Context,
                  private val mainModels: ArrayList<MainModel>,
                  private val itemClickListener: (Int) -> Unit
): androidx.recyclerview.widget.RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentModel = mainModels[position]
        holder.imageView.setImageResource(currentModel.logo)
        holder.textView.text = currentModel.nombre

        holder.itemView.setOnClickListener {
            itemClickListener(position)
        }

    }

    override fun getItemCount(): Int {
        return mainModels.size
    }

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val imageView: android.widget.ImageView = itemView.findViewById(R.id.image_view)
        val textView: android.widget.TextView = itemView.findViewById(R.id.text_view)
    }
}