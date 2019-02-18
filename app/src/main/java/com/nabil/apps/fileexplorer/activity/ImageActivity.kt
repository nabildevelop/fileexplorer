package com.nabil.apps.fileexplorer.activity

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.AppConstants.Companion.EXTRA_FILE_PATH
import com.nabil.apps.fileexplorer.FileTypeName
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.adapter.ImagePagerAdapter
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File

class ImageActivity: ThemeableAppCompatActivity(){
    lateinit var filePath: String
    lateinit var imagePagerAdapter: ImagePagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val color = ColorDrawable(theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).
                getColor(0, 0xffffeeeeee.toInt()))
        color.alpha = 51
        layout.background = color

        if(intent.hasExtra(EXTRA_FILE_PATH)){
            filePath = intent.getStringExtra(EXTRA_FILE_PATH)
            val file = File(filePath)

            val siblings = File(file.parent).listFiles().filter{f->
                val fileType = AppConstants.fileTypes.firstOrNull{f.extension.toLowerCase() in it.extensions}
                        (fileType!=null && fileType.name==FileTypeName.Image)}
            val imagePosition = siblings.indexOfFirst { it.path==filePath }
            imagePagerAdapter = ImagePagerAdapter(supportFragmentManager, siblings.toList())
            img_pager.adapter= imagePagerAdapter
            img_pager.currentItem = imagePosition
        }
    }


}