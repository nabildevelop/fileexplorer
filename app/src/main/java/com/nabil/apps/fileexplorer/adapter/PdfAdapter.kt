package com.nabil.apps.fileexplorer.adapter

import android.annotation.TargetApi
import android.content.Context
import android.view.ViewGroup
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.nabil.apps.fileexplorer.R
import kotlinx.android.synthetic.main.row_pdf.view.*
import kotlin.math.roundToInt


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PdfAdapter(var context: Context, var renderer: PdfRenderer) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    override fun getItemCount(): Int {
        return renderer.pageCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val page = renderer.openPage(holder.adapterPosition)
        val dm = context.resources.displayMetrics
        val scale = dm.widthPixels.toFloat() / page.width
        val bitmap = Bitmap.createBitmap(dm.widthPixels, (page.height*scale).roundToInt()
                , Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        holder.itemView.img_page?.setImageBitmap(bitmap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_pdf, parent, false)
        return object: RecyclerView.ViewHolder(view){}
    }
}