package com.nabil.apps.fileexplorer.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.SetPictureAsyncTask
import com.nabil.apps.fileexplorer.R
import kotlinx.android.synthetic.main.fragment_image.*
import java.lang.ref.WeakReference

class ImageFragment(): Fragment(){
    var imgMain: ImageView? = null
    lateinit var setPictureAsyncTask: SetPictureAsyncTask

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_image, null)
        imgMain = v.findViewById(R.id.img_main)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPictureAsyncTask = SetPictureAsyncTask()
        setPictureAsyncTask.execute(WeakReference(this), arguments!!.getString(AppConstants.EXTRA_FILE_PATH)!!)
    }
}