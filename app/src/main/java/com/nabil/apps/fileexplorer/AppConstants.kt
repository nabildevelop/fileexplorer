package com.nabil.apps.fileexplorer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.nabil.apps.fileexplorer.activity.*
import java.io.File
import java.lang.IllegalArgumentException


class AppConstants private constructor(){
    companion object {

        const val PERMISSION_CODE_WRITE_EXTERNAL_STORAGE: Int = 0
        const val PERMISSION_CODE_RECORD_AUDIO: Int = 1

        const val EXTRA_FILE_PATH = "filePath"

        const val PREF_KEY_SORT_BY = "sortBy"
        const val PREF_kEY_SHOW_HIDDEN = "showHidden"

        const val SORT_BY_NAME = 0
        const val SORT_BY_SIZE = 1
        const val SORT_BY_LAST_MODIFIED = 2

        val foldersFirstComparator = Comparator<FileHolder>{ o1, o2 -> if(o1.isDirectory==o2.isDirectory) 0 else if(o1.isDirectory)-1 else 1}

        private inline fun <reified T: Activity> openInActivity():(File, Context)->Unit{
            return { file, context ->
                val intent = Intent(context, T::class.java)
                intent.putExtra(EXTRA_FILE_PATH, file.path)
                context.startActivity(intent)
            }
        }

        val viewAction: (File, Context)->Unit = { file:File, context: Context->
            try{
                val fileUri = FileProvider.getUriForFile(context, "com.nabil.provider.fileexplorer",file)
                context.startActivity(Intent(Intent.ACTION_VIEW).apply{
                    setDataAndType(fileUri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.toLowerCase()))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                })
            } catch(e: IllegalArgumentException){
                Log.e("URI Error", "Couldn't get file URI")
            } catch (e: ActivityNotFoundException){
                Toast.makeText(context, R.string.couldnt_open_file, Toast.LENGTH_SHORT).show()
            }
        }

        val couldntOpenAction = { _:File, context:Context->
            Toast.makeText(context, R.string.couldnt_open_file, Toast.LENGTH_SHORT).show()
        }

        val themeIds = mapOf(
                "green" to R.style.AppThemeGreen,
                "orange" to R.style.AppThemeOrange,
                "purple" to R.style.AppThemePurple,
                "pink" to R.style.AppThemePink
        )

        val popupThemeIds = mapOf(
                "green" to R.style.PopupThemeGreen,
                "orange" to R.style.PopupThemeOrange,
                "purple" to R.style.PopupThemePurple,
                "pink" to R.style.PopupThemePink
        )

        val unknownFileType = FileType(FileTypeName.Unknown, arrayOf(), R.drawable.ic_insert_drive_file_black_24dp, couldntOpenAction)
        val fileTypes = arrayOf(
                FileType(FileTypeName.Image, arrayOf("png", "jpg", "jpeg"), R.drawable.ic_image_black_24dp, openInActivity<ImageActivity>(), true),
                FileType(FileTypeName.Video, arrayOf("mp4", "flv", "mkv", "3gp", "mpeg4"), R.drawable.ic_play_circle_filled_black_24dp, openInActivity<VideoActivity>()),
                FileType(FileTypeName.PDF, arrayOf("pdf"), R.drawable.ic_doc_pdf, openInActivity<PdfActivity>(), true),
                FileType(FileTypeName.TXT, arrayOf("txt"), R.drawable.ic_description_black_24dp, openInActivity<TextActivity>()),
                FileType(FileTypeName.APK, arrayOf("apk"), R.drawable.ic_android_black_24dp, viewAction, true),
                FileType(FileTypeName.Sound, arrayOf("mp3", "wma", "m4a", "ogg", "3gpp", "webm"), R.drawable.ic_doc_audio, openInActivity<SoundActivity>()),
                FileType(FileTypeName.Excel, arrayOf("xls", "xlsx"), R.drawable.ic_doc_excel, viewAction),
                FileType(FileTypeName.Word, arrayOf("doc", "docx"), R.drawable.ic_doc_word, viewAction),
                FileType(FileTypeName.PowerPoint, arrayOf("ppt", "pptx"), R.drawable.ic_doc_powerpoint, viewAction),
                FileType(FileTypeName.Html, arrayOf("xml", "html", "mhtml"), R.drawable.ic_doc_codes, viewAction),
                FileType(FileTypeName.archive, arrayOf("zip", "rar", "tg", "tar"), R.drawable.ic_doc_compressed, viewAction)
        )
    }
}