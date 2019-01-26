package com.nabil.apps.fileexplorer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.FileHolder
import com.nabil.apps.fileexplorer.GetPreviewAsyncTask
import com.nabil.apps.fileexplorer.R
import kotlinx.android.synthetic.main.item_file.view.*
import java.lang.ref.WeakReference

class FileAdapter(val context: Context, data  : Array<FileHolder>, private var sortBy: Int, private var showHidden: Boolean)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    val fetchingTasks =  ArrayList<GetPreviewAsyncTask>()
    var handleItemClick: ((FileHolder)->Unit)? = null
    var handleItemLongClick: ((FileHolder)->Unit)? = null
    private var mData: Array<FileHolder>

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val fileHolder = mData[holder.adapterPosition]
        holder.itemView.tx_file_name.text = mData[holder.adapterPosition].file.name
        if(fileHolder.isDirectory)
            holder.itemView.img_icon.setImageResource(R.drawable.ic_folder_yellow_24dp)
        else
            fileHolder.preview?.let{holder.itemView.img_icon.setImageBitmap(it)}?:holder.itemView.img_icon.setImageResource(fileHolder.type.iconId)

        // fetch and display preview if available
        if(fileHolder.type.previewable && fileHolder.preview==null && fetchingTasks.none{it.fileHolder==fileHolder}){
            val task = GetPreviewAsyncTask(WeakReference(context), fileHolder){
                fileHolder.preview = it
                notifyItemChanged(holder.adapterPosition)
            }
            task.execute()
            fetchingTasks.add(task)
        }

        // handle click events
        holder.itemView.setOnClickListener{
            handleItemClick?.invoke(mData[holder.adapterPosition])
        }
        holder.itemView.setOnLongClickListener{
            handleItemLongClick?.invoke(mData[holder.adapterPosition])
            true
        }
    }


    init {
        mData = data
        sortData(sortBy)
        if(!showHidden){
            mData.filter{!it.file.isHidden}
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


    fun sort(sortBy: Int){
        this.sortBy = sortBy
        sortData(sortBy)
        notifyDataSetChanged()
    }

}