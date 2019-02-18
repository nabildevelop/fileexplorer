package com.nabil.apps.fileexplorer.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class ColorChooserAdapter(var data: IntArray): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var handleItemClick: (item: Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = View(parent.context)
        val lp = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, 80)
        lp.setMargins(8,8,8,8)
        view.layoutParams = lp
        return object: RecyclerView.ViewHolder(view){}.apply   {
                itemView.setOnClickListener {
                    handleItemClick(data[adapterPosition])
                }
        }
    }
    override fun getItemCount(): Int {return data.size }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setBackgroundColor(data[holder.adapterPosition])
    }
}