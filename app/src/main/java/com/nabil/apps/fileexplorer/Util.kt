package com.nabil.apps.fileexplorer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfRenderer
import android.os.AsyncTask
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.math.roundToInt




class GetPreviewAsyncTask(private val contextRef: WeakReference<Context>, val fileHolder: FileHolder, val callback: (Bitmap?)->Unit): AsyncTask<String, Int, Bitmap?>(){
    override fun doInBackground(vararg params: String): Bitmap? {
        try{
            when(fileHolder.type.name){
                FileTypeName.Image-> return getImagePreview(fileHolder.path)
                FileTypeName.PDF-> return getPdfPreview(fileHolder.path)
                FileTypeName.APK-> return getApkPreview(fileHolder.path, contextRef.get())
            }
        }catch(ex: IOException){
            ex.printStackTrace()
            cancel(true)
            return null
        }
        return null
    }

    override fun onPostExecute(result: Bitmap?) {
        callback(result)
    }


}


fun getImagePreview(imgPath: String): Bitmap{
    val targetW = 120
    val targetH = 100

    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(imgPath, bmOptions)
    val photoW = bmOptions.outWidth
    val photoH = bmOptions.outHeight

    val scaleFactor = Math.min(photoW/ targetW, photoH/ targetH)

    bmOptions.inJustDecodeBounds = false
    bmOptions.inSampleSize = scaleFactor
    bmOptions.inPurgeable = true

    return BitmapFactory.decodeFile(imgPath, bmOptions)
}

fun getPdfPreview(pdfPath: String): Bitmap?{
    val fileDescriptor= ParcelFileDescriptor.open(File(pdfPath),ParcelFileDescriptor.MODE_READ_ONLY)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        var page: PdfRenderer.Page? = null
        try{
            val renderer = PdfRenderer(fileDescriptor)
            // Open first page as a preview
            page = renderer.openPage(0)
            val scale = 160f / page.width
            val bitmap = Bitmap.createBitmap(160, (page.height*scale).roundToInt()
                    , Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return bitmap
        }catch(ex:IOException){
            ex.printStackTrace()
            return null
        }
        finally{
            page?.close()
        }
    } else {
        TODO("VERSION.SDK_INT < LOLLIPOP")
    }
}

fun getApkPreview(apkPath: String, context: Context?): Bitmap?{
    if(context==null)
        return null
    val packageInfo = context.packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES)
    if(packageInfo!=null){
        val appInfo = packageInfo.applicationInfo
        appInfo.sourceDir = apkPath
        appInfo.publicSourceDir = apkPath
        val icon = appInfo.loadIcon(context.packageManager)
        return (icon as BitmapDrawable).bitmap
    }
    return null
}


fun formatSeconds(seconds: Int): String{
    return (seconds/60).toString().padStart(2, '0') + ":" +
            (seconds%60).toString().padStart(2, '0')
}

inline fun <reified T: Activity> startActivity(context: Context){
    context.startActivity(Intent(context, T::class.java))
}

