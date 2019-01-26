package com.nabil.apps.fileexplorer.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.R
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment(): Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(arguments!=null && arguments!!.containsKey(AppConstants.EXTRA_FILE_PATH)){
            setPic(arguments!!.getString(AppConstants.EXTRA_FILE_PATH)!!)
        }
    }

    private fun setPic(imgPath: String){
        val targetW = img_main.layoutParams.width
        val targetH = img_main.layoutParams.height

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imgPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        val scaleFactor = Math.min(photoW/ targetW, photoH/ targetH)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true

        val bitmap = BitmapFactory.decodeFile(imgPath, bmOptions)
        img_main.setImageBitmap(bitmap)
    }
}