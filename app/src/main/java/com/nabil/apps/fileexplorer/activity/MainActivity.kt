package com.nabil.apps.fileexplorer.activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.AppConstants.Companion.PERMISSION_CODE_WRITE_EXTERNAL_STORAGE
import com.nabil.apps.fileexplorer.AppConstants.Companion.PREF_KEY_SORT_BY
import com.nabil.apps.fileexplorer.AppConstants.Companion.PREF_kEY_SHOW_HIDDEN
import com.nabil.apps.fileexplorer.AppData
import com.nabil.apps.fileexplorer.FileHolder
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.dialog.CreateFileDialog
import kotlinx.android.synthetic.main.activity_main.*
import com.nabil.apps.fileexplorer.adapter.FileAdapter
import com.nabil.apps.fileexplorer.adapter.PathAdapter
import kotlinx.android.synthetic.main.bottomsheet_file_details.view.*
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*


class MainActivity : ThemeableAppCompatActivity(){

    lateinit var prefs: SharedPreferences


    lateinit var fileAdapter: FileAdapter
    lateinit var layoutManager: GridLayoutManager
    var sortBy = AppConstants.SORT_BY_NAME
    var isInActionMode = false

    val scrollStateStack =  Stack<Parcelable>()
    val folderStack = Stack<FileHolder>()
    lateinit var pathAdapter: PathAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)



        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        sortBy = prefs.getInt(PREF_KEY_SORT_BY, AppConstants.SORT_BY_NAME)

        toolbar_action_mode.inflateMenu(R.menu.menu_main_action_mode)
        toolbar_action_mode.setOnMenuItemClickListener{menuItem->
            when(menuItem.itemId){
                R.id.menu_item_info->showDetails(fileAdapter.selectedItems[0].file)
                R.id.menu_item_rename->showRenameDialog()
                R.id.menu_item_share->shareFiles(fileAdapter.selectedItems.map{it.file})
                R.id.menu_item_delete->showDeleteDialog()
            }
            true
        }
        toolbar_action_mode.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar_action_mode.setNavigationOnClickListener { if(isInActionMode)finishActionMode() }

        val color = ColorDrawable(theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).
                getColor(0, 0xffffeeeeee.toInt()))
        color.alpha = 51
        coord_layout.background = color

        if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage("File Explorer needs to access phone's external storage.\n" +
                        "Please grant the app permission").setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                }.show()
            }
            else{
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
            }
        }
        else{
            showRoot()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE_WRITE_EXTERNAL_STORAGE ->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    showRoot()
                }
                else{
                    TODO("Handle user denying later")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.menu_item_show_hidden_files).isChecked = prefs.getBoolean(PREF_kEY_SHOW_HIDDEN, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_item_new_file->{
                CreateFileDialog.newInstance(AppData.instance.currentFolder.path, false).show(supportFragmentManager, "NewFile")
            }
            R.id.menu_item_new_folder->{
                CreateFileDialog.newInstance(AppData.instance.currentFolder.path, true).show(supportFragmentManager, "NewFolder")
            }
            R.id.menu_item_sort->{
                showSortByDialog()
            }
            R.id.menu_item_show_hidden_files->{
                val showHidden = !item.isChecked
                prefs.edit().putBoolean(PREF_kEY_SHOW_HIDDEN, showHidden).apply()
                fileAdapter.showHidden = showHidden
            }
            R.id.menu_item_settings ->{
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showRoot(){
        showFiles(AppData.instance.currentFolder)
        folderStack.push(AppData.instance.currentFolder)
        pathAdapter = PathAdapter(folderStack)
        pathAdapter.clickHandler = {fileHolder, position->
            if(position!=folderStack.size-1){
                val priorSize=folderStack.size
                while(folderStack.size>position+1)
                    folderStack.pop()
                pathAdapter.notifyItemRangeRemoved(position+1, priorSize-position-1)
                pathAdapter.notifyItemChanged(folderStack.size-1)
                showFiles(folderStack.peek())
            }
        }
        rv_path.adapter = pathAdapter
        rv_path.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false)
    }


    private fun showFiles(folder: FileHolder){
        AppData.instance.currentFolder = folder
        folder.listChildren().apply{
            fileAdapter = FileAdapter(this@MainActivity, arrayListOf<FileHolder>().also{it.addAll(this)}, sortBy, prefs.getBoolean(PREF_kEY_SHOW_HIDDEN, false), tx_empty_folder)
            rv_files.adapter = fileAdapter
            fileAdapter.handleItemClick = {fileHolder, position->
                if(isInActionMode){
                    fileHolder.selected=!fileHolder.selected
                    fileAdapter.notifyItemChanged(position)
                    fileAdapter.selectedCount.also{
                        if(it==0)
                            finishActionMode()
                        else
                            toolbar_action_mode.title = "$it"
                    }
                    refreshActionMenu()
                }
                else if(!fileHolder.file.isDirectory)fileHolder.open(this@MainActivity)
                else{
                    showFiles(fileHolder)
                    folderStack.push(fileHolder)
                    pathAdapter.notifyItemRangeChanged(folderStack.size-2, 2)
                    rv_path.scrollToPosition(folderStack.size-1)
                }
            }
            fileAdapter.handleItemLongClick = {fileHolder, position->
                if(!isInActionMode){
                    startActionMode()
                    fileHolder.selected= true
                    fileAdapter.notifyItemChanged(position)
                    refreshActionMenu()
                }else{
                    fileHolder.selected=!fileHolder.selected
                    fileAdapter.notifyItemChanged(position)
                    fileAdapter.selectedCount.also{
                        if(it==0)
                            finishActionMode()
                        else
                            toolbar_action_mode.title = "$it"
                    }
                    refreshActionMenu()
                }
            }

            val columns = ((resources.displayMetrics.widthPixels / resources.displayMetrics.density) / 120).toInt()
            layoutManager = GridLayoutManager(this@MainActivity, columns)
            rv_files.layoutManager = layoutManager

        }
    }

    private fun showSortByDialog() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setSingleChoiceItems(R.array.sort_by_options, sortBy) { dialog, index ->
            sortBy = index
            fileAdapter.sortBy = sortBy
            prefs.edit().putInt(PREF_KEY_SORT_BY, sortBy).apply()
            dialog.dismiss()
        }.setTitle(R.string.sort_by).show()
    }


    private fun startActionMode(){
        isInActionMode = true
        layout_action_mode.visibility = View.VISIBLE
        toolbar.visibility = View.GONE
        toolbar_action_mode.title = "1"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = 0xff444444.toInt()
        }
    }
    private fun finishActionMode(){
        isInActionMode = false
        layout_action_mode.visibility = View.GONE
        toolbar.visibility = View.VISIBLE
        fileAdapter.unselectAll()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimaryDark)).getColor(0, 0xffffeeee.toInt())
        }
    }

    override fun onBackPressed() {
        if(isInActionMode)
            finishActionMode()
        else {
            if(AppData.instance.currentFolder.path!=AppData.instance.rootFile.path) {
                AppData.instance.currentFolder.parent?.let { showFiles(it) }
                folderStack.pop()
                pathAdapter.notifyItemRemoved(folderStack.size)
                pathAdapter.notifyItemChanged(folderStack.size-1)
            }
            else {
                super.onBackPressed()
            }
        }
    }

    fun showDeleteDialog(){
        AlertDialog.Builder(this).setTitle(R.string.delete).setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete) {_,_->deleteFiles()}.setNegativeButton(android.R.string.cancel, null).create().show()
    }

    fun deleteFiles(){
        fileAdapter.selectedItems.forEach{deleteFileRecusrsively(it.file)}
        fileAdapter.data = ArrayList<FileHolder>().also{it.addAll(AppData.instance.currentFolder.listChildren())}
        Snackbar.make(coord_layout, R.string.files_deleted, Snackbar.LENGTH_SHORT).show()
        finishActionMode()
    }

    private fun deleteFileRecusrsively(file: File){
        if(file.isDirectory)
            file.listFiles().forEach { deleteFileRecusrsively(it) }
        file.delete()
    }

    private fun shareFiles(filesToSend: List<File>){
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.")
        intent.type = "*/*"
        val uris = ArrayList<Uri>()
        for (file in filesToSend) {
            val fileUri: Uri? = try{
                FileProvider.getUriForFile(this, "com.nabil.provider.fileexplorer",file)
            } catch(e: IllegalArgumentException){
                Log.e("File Sharer", "Some of the selected files can't be shared; $file")
                null
            }
            uris.add(fileUri!!)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        val chooser = Intent.createChooser(intent, getString(R.string.send_files))
        try{
            startActivity(chooser)
        }
        catch(ex: ActivityNotFoundException){
            Log.e("Couldn't handle intent", "No activity found to handle the intent")
            ex.printStackTrace()
        }
    }

    fun refreshActionMenu(){
        val single = fileAdapter.selectedCount==1
        toolbar_action_mode.menu.findItem(R.id.menu_item_info).apply{isEnabled=single; icon.alpha=if(single)255 else 130}
        toolbar_action_mode.menu.findItem(R.id.menu_item_rename).apply{isEnabled=single; icon.alpha=if(single)255 else 130}
        toolbar_action_mode.menu.findItem(R.id.menu_item_share).also{
            fileAdapter.selectedItems.none { it.isDirectory }.also{enabled->it.isEnabled=enabled;it.icon.alpha=if(enabled)255 else 130}}
    }

    fun showDetails(file: File){
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setTitle(R.string.file_details)
        val view = layoutInflater.inflate(R.layout.bottomsheet_file_details, null)
        view.tx_file_name.text= file.name
        view.tx_file_path.text= file.path
        view.tx_file_type.text=if(file.isDirectory)"Directory" else "File"
        view.tx_last_modified.text= Date(file.lastModified()).toString()
        view.tx_file_size.text= ""+file.length()
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    fun showRenameDialog(){
        val editText = EditText(this)
        editText.append(fileAdapter.selectedItems[0].file.name)
        AlertDialog.Builder(this).setTitle(R.string.rename).setMessage(R.string.enter_file_name)
                .setView(editText).setPositiveButton(R.string.rename){_,_->}
                .setNegativeButton(android.R.string.cancel){_,_->}
                .create().show()
    }

}
