package com.nabil.apps.fileexplorer.activity

import android.animation.ObjectAnimator
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nabil.apps.fileexplorer.AppConstants.Companion.EXTRA_FILE_PATH
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.adapter.PdfAdapter
import kotlinx.android.synthetic.main.activity_pdf.*
import java.io.File
import java.io.IOException

class PdfActivity : ThemeableAppCompatActivity() {

    lateinit var pageNumFadeAnimator: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)


        pageNumFadeAnimator = ObjectAnimator.ofFloat(page_number, "alpha", 1f, 0f)
                .apply { duration=600; startDelay=1000 }

        val fileDescriptor = ParcelFileDescriptor.open(File(intent.getStringExtra(EXTRA_FILE_PATH)),
                ParcelFileDescriptor.MODE_READ_ONLY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try{
                val renderer = PdfRenderer(fileDescriptor)
                val adapter = PdfAdapter(this@PdfActivity, renderer)
                rv_pages.adapter = adapter
                rv_pages.layoutManager = LinearLayoutManager(this@PdfActivity)
                rv_pages.addOnScrollListener(scrollListener)
            }
            catch(ex: IOException){
                ex.printStackTrace()
                Toast.makeText(this@PdfActivity, "Couldn't open pdf file", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
    }

    val scrollListener = object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val fvip =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            page_number.text = "" + (fvip +1)
        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if(newState== RecyclerView.SCROLL_STATE_DRAGGING){
                pageNumFadeAnimator.cancel()
                page_number.alpha = 1.0f
            }
            if(newState==RecyclerView.SCROLL_STATE_IDLE){
                pageNumFadeAnimator.start()
            }
        }
    }
}
