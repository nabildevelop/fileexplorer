/**

package com.nabil.apps.fileexplorer.adapter


import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nabil.apps.fileexplorer.*
import com.nabil.apps.fileexplorer.FileHolder.Companion.FETCHED
import com.nabil.apps.fileexplorer.FileHolder.Companion.FETCHING
import com.nabil.apps.fileexplorer.FileHolder.Companion.NOT_FETCHED
import kotlinx.android.synthetic.main.row_tree.view.*
import java.io.File

class FileTreeAdapter(data: Array<File>, private var sortBy: Int, var showHidden: Boolean) {

    companion object {
        private const val OFFSET = 50
        const val SORT_BY_NAME = 0
        const val SORT_BY_SIZE = 1
        const val SORT_BY_LAST_MODIFIED = 2

        val foldersFirstComparator = Comparator<File>{ o1, o2 -> if(o1.isDirectory==o2.isDirectory) 0 else if(o1.isDirectory)-1 else 1}
    }


    init{
        sort(sortBy)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_tree, parent, false)
        return object: RecyclerView.ViewHolder(itemView){}
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val item = mData[holder.adapterPosition]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            holder.itemView.layout.setPaddingRelative(item.depth*OFFSET, 0, 0, 0)
        else
            holder.itemView.layout.setPadding(item.depth*OFFSET, 0, 0, 0)

        val expandable = if(showHidden) item.children.any()
                else item.children.any { !it.file.isHidden }
        holder.itemView.img_arrow.visibility = if(expandable) View.VISIBLE else View.INVISIBLE
        holder.itemView.img_arrow.setImageResource(if(item.expanded)
            R.drawable.ic_keyboard_arrow_down_black_24dp else R.drawable.ic_keyboard_arrow_right_black_24dp)


        holder.itemView.layout.setOnClickListener {
            toggleExpansion(holder.adapterPosition)
        }
    }


    private fun flattenData(depth: Int, data: ArrayList<FileHolder>, sortBy: Int, showHidden: Boolean): ArrayList<FileHolder>{
        val flattenedData = ArrayList<FileHolder>()
        when(sortBy){
            SORT_BY_NAME->data.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) {it.file.name})
            SORT_BY_LAST_MODIFIED->data.sortBy { it.file.lastModified() }
            SORT_BY_SIZE->data.sortBy { it.file.length() }
        }
        data.sortWith(foldersFirstComparator)
        for(i in 0 until data.size){
            val item = data[i]
            item.depth = depth
            if(showHidden || !item.file.isHidden){
                flattenedData.add(item)
                if(item.expanded)
                    flattenedData.addAll(flattenData(depth+1, ArrayList(item.children), sortBy, showHidden))
            }
        }
        return flattenedData
    }

    private fun toggleExpansion(position: Int){
        val item = mData[position]
        if(item.expanded)
            fold(position)
        else
            expand(position)
    }

    private fun expand(position: Int){
        val item = mData[position]
        when(sortBy){
            SORT_BY_NAME->item.children.sortBy{ it.file.name  }
            SORT_BY_LAST_MODIFIED->item.children.sortBy { it.file.lastModified() }
            SORT_BY_SIZE->item.children.sortBy { it.file.length() }
        }
        item.children.sortWith(foldersFirstComparator)
        var addedChildrenCount=0
        for(i in 0 until item.children.size)
        {
            val child = item.children[i]
            child.depth = item.depth + 1
            if(!child.childrenFetched)
                child.fetchChildren()
            if(showHidden || !child.file.isHidden){
                mData.add(position+1+addedChildrenCount, child)
                addedChildrenCount++
            }
        }
        notifyItemChanged(position)
        notifyItemRangeInserted(position+1, addedChildrenCount)

        // Mark item as expanded
        item.expanded = true
    }

    private fun fold(position: Int){
        val item = mData[position]
        val itemDepth = item.depth
        val removalPosition = position+1
        var counter = 0
        while(removalPosition<mData.size){
            val child = mData[removalPosition]
            if(child.depth>itemDepth)
                mData.removeAt(removalPosition)
            else
                break
            counter+=1
        }
        notifyItemChanged(position)
        notifyItemRangeRemoved(removalPosition, counter)

        // Mark item and its children as not expanded
        item.setExpandedRecursively(false)
    }

    fun showHiddenFiles(show: Boolean){

    }

     override fun sort(sortBy: Int){
        this.sortBy = sortBy
        mData = flattenData(0, data, sortBy, showHidden)
        notifyDataSetChanged()
    }
}

 **/