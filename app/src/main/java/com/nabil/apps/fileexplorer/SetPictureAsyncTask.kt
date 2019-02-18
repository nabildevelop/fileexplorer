package com.nabil.apps.fileexplorer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.nabil.apps.fileexplorer.fragment.ImageFragment
import java.lang.ref.WeakReference
import kotlinx.android.synthetic.main.fragment_image.*
import java.lang.IllegalStateException
import java.lang.NullPointerException

class SetPictureAsyncTask() : AsyncTask<Any, Int, Bitmap?>() {
    lateinit var imgFragmentRef: WeakReference<ImageFragment>
    lateinit var imgPath: String


    override fun doInBackground(vararg params: Any?): Bitmap? {
        try{
            imgFragmentRef = params[0] as WeakReference<ImageFragment>
            imgPath = params[1] as String
            val targetW = imgFragmentRef.get()?.imgMain?.layoutParams?.width?:10
            val targetH = imgFragmentRef.get()?.imgMain?.layoutParams?.height?:10

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imgPath, bmOptions)
            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight

            val scaleFactor = Math.min(photoW/ targetW, photoH/ targetH)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(imgPath, bmOptions)
        }catch(oome: OutOfMemoryError){
            Log.e("OutOfMemoryError", oome.stackTrace.toString())
        }
        return null
    }

    override fun onPostExecute(result: Bitmap?) {
        try{
            val fragment = imgFragmentRef.get()
            fragment?.progress?.visibility=View.GONE
            result?.let{fragment?.imgMain?.setImageBitmap(it)}
                    ?:run{fragment?.imgMain?.setImageResource(R.drawable.ic_broken_image_black_24dp)}
        }catch(npe: NullPointerException){
            Log.e("NullPointerException", npe.stackTrace.toString())
        }catch(ise: IllegalStateException){
            Log.e("IllegalStateException", ise.stackTrace.toString())
        }
    }
}