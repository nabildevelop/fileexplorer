package com.nabil.apps.fileexplorer

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.lang.ref.WeakReference

class FileHolder(val file: File){
    var preview: Bitmap?=null
    var parent = if(file.parentFile==null)null else FileHolder(file.parentFile)
    val isDirectory = file.isDirectory
    val path = file.path
    fun listChildren(): Array<FileHolder>{
        return file.listFiles().map{FileHolder(it)}.toTypedArray()
    }
    val type: FileType = AppConstants.fileTypes.firstOrNull{it.extensions.contains(file.extension)}?:AppConstants.unknownFileType
    fun open(context: Context){
        type.openFile(file, context)
    }
}