package com.nabil.apps.fileexplorer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.AppConstants.Companion.EXTRA_FILE_PATH
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.adapter.ColorChooserAdapter
import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.android.synthetic.main.dialog_text_color.view.*
import java.io.*


class TextActivity: ThemeableAppCompatActivity() {

    companion object {
        const val MIN_FONT_SIZE = 8f
        const val MAX_FONT_SIZE = 99f
    }

    lateinit var text: String
    lateinit var file: File

    var editMode = false

    var textColor = 0xff000000.toInt()
        set(value){
            field = value
            edit_text.setTextColor(value)
            tx_text.setTextColor(value)
        }

    var currentTextSize = 20f
        set(value) {
            if(value in MIN_FONT_SIZE..MAX_FONT_SIZE){
                field = value
                edit_font_size.text?.clear()
                edit_font_size.text?.insert(0, ""+value)
                btn_increase_font.isEnabled = value<MAX_FONT_SIZE
                btn_decrease_font.isEnabled = value>MIN_FONT_SIZE
                edit_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
                tx_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            }
        }


    var typeFace: Typeface = Typeface.DEFAULT
        set(value){
            field = value
            edit_text.setTypeface(value, typeFaceStyle)
            tx_text.setTypeface(value, typeFaceStyle)
        }

    private var typeFaceStyle: Int = Typeface.NORMAL
        set(value){
            field = value
            edit_text.setTypeface(typeFace, value)
            tx_text.setTypeface(typeFace, value)
            btn_text_bold.setColorFilter(if(value and Typeface.BOLD ==0)0xff888888.toInt() else 0xff000000.toInt())
            btn_text_italic.setColorFilter(if(value and Typeface.ITALIC == 0)0xff888888.toInt() else 0xff000000.toInt())
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeName = PreferenceManager.getDefaultSharedPreferences(this).
                getString(getString(R.string.pref_key_theme), "green")!!
        setTheme(AppConstants.themeIds[themeName]!!)
        setContentView(R.layout.activity_text)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

        val path = if(intent.hasExtra(EXTRA_FILE_PATH))intent.getStringExtra(EXTRA_FILE_PATH)
                        else intent.data!!.path
        showFile(path)
        setUpTextFormatLayout()

        currentTextSize = 20f
        typeFaceStyle = Typeface.NORMAL
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(if(editMode)R.menu.menu_text_edit else R.menu.menu_text, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId){
            android.R.id.home->{
                if(editMode)
                    switchEditMode(false)
                else
                    finish()
            }
            R.id.menu_item_edit->{
                switchEditMode(true)
            }
            R.id.menu_item_text_format->{
                scroll_text_format.visibility = if(scroll_text_format.visibility!= View.VISIBLE)
                    View.VISIBLE else View.GONE
            }
            R.id.menu_item_settings->{
                startActivity(Intent(this@TextActivity, SettingsActivity::class.java))
            }
            R.id.menu_item_save->{
                saveText()
                switchEditMode(false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(editMode)
            switchEditMode(false)
        else
            super.onBackPressed()
    }

    fun showFile(path: String){
        file = File(path)
        title = file.name
        val fis = FileInputStream(file)
        val isr = InputStreamReader(fis, "UTF-8")
        val br = BufferedReader(isr)
        val sb = StringBuilder()

        br.forEachLine { l -> sb.append(l) }

        text = sb.toString()

        tx_text.text = text
        tx_text.requestFocus()
    }

    private fun setUpTextFormatLayout(){
        btn_increase_font.setOnClickListener {currentTextSize++}
        btn_decrease_font.setOnClickListener {currentTextSize--}
        btn_text_color.setOnClickListener {displayColorChooserDialog()}
        btn_text_bold.setOnClickListener {typeFaceStyle = typeFaceStyle xor Typeface.BOLD }
        btn_text_italic.setOnClickListener {typeFaceStyle = typeFaceStyle xor Typeface.ITALIC}
        edit_font_size.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(edit_font_size.text!!.isNotEmpty()){
                    val size = java.lang.Float.parseFloat(edit_font_size.text.toString())
                    // set currentTextSize only if size is different to prevent circular loop
                    if(size!=currentTextSize){
                        currentTextSize=size
                    }
                }
            }
        })
        edit_font_size.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(!hasFocus)
                if((v as EditText).text.isEmpty()||
                        java.lang.Float.parseFloat(v.text.toString())!=currentTextSize) {
                    v.text.clear()
                    v.text.insert(0, ""+currentTextSize)
                }
        }

        spinner_typeface.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0-> typeFace = Typeface.DEFAULT
                    1-> typeFace = Typeface.SANS_SERIF
                    2-> typeFace = Typeface.SERIF
                    3-> typeFace = Typeface.MONOSPACE
                }
            }
        }

    }

    fun displayColorChooserDialog(){
        val view = LayoutInflater.from(this@TextActivity).inflate(R.layout.dialog_text_color, null)
        val builder = AlertDialog.Builder(this@TextActivity)
        val dialog= builder.setView(view).setTitle(R.string.choose_text_color).create()
        val colorArray = resources.getIntArray(R.array.colors)
        val adapter = ColorChooserAdapter(colorArray)
        adapter.handleItemClick = {textColor=it;dialog.dismiss()}
        view.rv_colors.adapter = adapter
        view.rv_colors.layoutManager = GridLayoutManager(this@TextActivity, 5)
        dialog.show()
    }

    private fun switchEditMode(editMode: Boolean){
        this.editMode = editMode
        if(editMode){
            tx_text.visibility = View.GONE
            edit_text.visibility = View.VISIBLE
            edit_text.text!!.clear()
            edit_text.text!!.insert(0, text)
            edit_text.requestFocus()
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(edit_text, InputMethodManager.SHOW_IMPLICIT)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp)
            toolbar.setBackgroundColor(0xffbbbbbb.toInt())
        }
        else{
            edit_text.visibility = View.GONE
            tx_text.visibility = View.VISIBLE
            tx_text.text = text
            tx_text.requestFocus()
            (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).
                    hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
            val primaryColor = theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).getColor(0, 0xffffddaa.toInt())
            toolbar.setBackgroundColor(primaryColor)
        }
        invalidateOptionsMenu()
    }

    private fun saveText(){
        try{
            val newText = edit_text.text.toString()
            val fos = FileOutputStream(file)
            val osw = OutputStreamWriter(fos, "UTF-8")
            val bw = BufferedWriter(osw)
            bw.write(newText)
            bw.flush()
            bw.close()
            text = newText
            Snackbar.make(coord_layout, R.string.changes_saved, Snackbar.LENGTH_SHORT).show()
        }
        catch(ex: IOException){
            ex.printStackTrace()
        }
    }
}

