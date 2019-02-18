package com.nabil.apps.fileexplorer

import android.os.Environment

class AppData private constructor() {

    companion object {
        val instance = AppData()
    }

    val rootFile : FileHolder by lazy{
        FileHolder(Environment.getExternalStorageDirectory())
    }

    var currentFolder: FileHolder = rootFile
}