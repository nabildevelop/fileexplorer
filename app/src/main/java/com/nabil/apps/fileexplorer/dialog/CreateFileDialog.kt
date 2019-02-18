package com.nabil.apps.fileexplorer.dialog


import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.nabil.apps.fileexplorer.AppConstants.Companion.PREF_kEY_SHOW_HIDDEN
import com.nabil.apps.fileexplorer.AppData
import com.nabil.apps.fileexplorer.FileHolder
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.activity.MainActivity
import com.nabil.apps.fileexplorer.adapter.FileAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.ArrayList

class CreateFileDialog : DialogFragment() {
    lateinit var mainActivity: MainActivity

    companion object {
        const val ARG_DIRECTORY = "directory"
        const val ARG_PATH = "path"
        fun newInstance(path: String, isDirectory: Boolean): CreateFileDialog{
            return CreateFileDialog().apply{arguments=Bundle().apply{putString(ARG_PATH, path);putBoolean(ARG_DIRECTORY,isDirectory)}}
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mainActivity = activity as MainActivity
        val builder = AlertDialog.Builder(context!!)
        val path: String = arguments!!.getString(ARG_PATH, AppData.instance.rootFile.file.path)
        val isDirectory: Boolean = arguments?.getBoolean(ARG_DIRECTORY)?:false
        val title = if(isDirectory) R.string.new_folder else R.string.new_file
        val message = if(isDirectory) R.string.enter_folder_name else R.string.enter_file_name
        builder.setMessage(message)
        builder.setTitle(title)
        val editText = EditText(context)
        builder.setView(editText)
        builder.setPositiveButton(if(isDirectory) R.string.create_folder else R.string.create_file) { _, _->
            val file = File(path+"/"+editText.text.toString())
            try{
                if(isDirectory)
                    file.mkdir()
                else
                    file.createNewFile()
                mainActivity.fileAdapter.data = ArrayList<FileHolder>().also{it.addAll(AppData.instance.currentFolder.listChildren())}

                Toast.makeText(context, if(isDirectory)R.string.folder_created else R.string.file_created, Toast.LENGTH_SHORT).show()
            }
            catch(ex: IOException){
                ex.printStackTrace()
                Toast.makeText(context, R.string.provide_valid__name, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }
}