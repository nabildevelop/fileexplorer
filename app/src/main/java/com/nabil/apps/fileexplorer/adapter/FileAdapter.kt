package com.nabil.apps.fileexplorer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.FileHolder
import com.nabil.apps.fileexplorer.R
import kotlinx.android.synthetic.main.item_file.view.*

class FileAdapter(val context: Context, data  : ArrayList<FileHolder>, sortBy: Int, showHidden: Boolean, var emptyView: View?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var handleItemClick: ((FileHolder, Int)->Unit)? = null
    var handleItemLongClick: ((FileHolder, Int)->Unit)? = null

    val selectedCount get() = mData.count{it.selected}
    val selectedItems get() = mData.filter{it.selected}


    private val mData = mutableListOf<FileHolder>().apply{addAll(data)}

    init{
        sortData(sortBy)
        if(!showHidden)
            mData.retainAll{!it.file.isHidden}
        checkEmpty()
        registerAdapterDataObserver(object:RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                checkEmpty()
            }
        })
    }

    var data = data
        set(value){
            if(field!=value){
                field=value
                mData.clear()
                mData.addAll(data)
                if(!showHidden)
                    mData.retainAll{!it.file.isHidden}
                sortData(sortBy)
                notifyDataSetChanged()
            }
        }



    var sortBy = sortBy
        set(value) {
            if(field!=value){
                field=value
                sortData(value)
                notifyDataSetChanged()
            }
        }

    var showHidden = showHidden
        set(value) {
            if(field!=value){
                field=value
                mData.clear()
                if(value)
                    mData.addAll(data)
                else
                    mData.addAll(data.filter{!it.file.isHidden})
                sortData(sortBy)
                notifyDataSetChanged()
            }
        }

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val fileHolder = mData[holder.adapterPosition]
        holder.itemView.tx_file_name.text = fileHolder.file.name
        holder.itemView.view_edge.visibility=if(fileHolder.selected) View.VISIBLE else View.GONE
        holder.itemView.img_selected_check.visibility=if(fileHolder.selected) View.VISIBLE else View.GONE
        if(fileHolder.isDirectory)
            holder.itemView.img_icon.setImageResource(R.drawable.ic_folder_yellow_24dp)
        else
            fileHolder.preview?.let{holder.itemView.img_icon.setImageBitmap(it)}?:holder.itemView.img_icon.setImageResource(fileHolder.type.iconId)

        // fetch and display preview if available


        // handle click events
        holder.itemView.setOnClickListener{
            handleItemClick?.invoke(fileHolder, holder.adapterPosition)
        }
        holder.itemView.setOnLongClickListener{
            handleItemLongClick?.invoke(fileHolder, holder.adapterPosition)
            true
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_file, parent, false)){}

    private fun sortData(sortBy: Int){
        when(sortBy){
            AppConstants.SORT_BY_NAME ->mData.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) {it.file.name})
            AppConstants.SORT_BY_LAST_MODIFIED ->mData.sortBy { it.file.lastModified() }
            AppConstants.SORT_BY_SIZE ->mData.sortBy { it.file.length() }
        }
        mData.sortWith(AppConstants.foldersFirstComparator)
    }

    fun unselectAll(){
        mData.filter{it.selected}.forEach{it.selected=false}
        notifyDataSetChanged()
    }

    fun checkEmpty(){
        emptyView?.visibility = if(mData.isEmpty())View.VISIBLE else View.GONE
    }

}