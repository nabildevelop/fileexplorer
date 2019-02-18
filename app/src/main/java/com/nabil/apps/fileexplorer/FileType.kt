package com.nabil.apps.fileexplorer

import android.content.Context
import java.io.File


class FileType(val name: FileTypeName, val extensions: Array<String>,
               val iconId: Int, val openFile: (file: File, context:Context) -> Unit, val previewable: Boolean = false)
