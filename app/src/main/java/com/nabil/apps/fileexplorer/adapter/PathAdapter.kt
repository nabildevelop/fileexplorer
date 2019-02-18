package com.nabil.apps.fileexplorer.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nabil.apps.fileexplorer.FileHolder
import com.nabil.apps.fileexplorer.R
import kotlinx.android.synthetic.main.item_path.view.*
import java.util.*

class PathAdapter(var data: Stack<FileHolder>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var clickHandler: ((FileHolder, Int)->Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object:RecyclerView.ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_path, null)){}
    }
    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val pos = holder.adapterPosition
        val f = data[pos]
        holder.itemView.tx_file_name.text = f.name
        holder.itemView.img_arrow.visibility = if(pos==0) View.GONE else View.VISIBLE
        holder.itemView.tx_file_name.setTypeface(
                Typeface.DEFAULT,if(pos==data.size-1) Typeface.BOLD else Typeface.NORMAL)
        holder.itemView.setOnClickListener{clickHandler?.invoke(f, pos)}
    }
}