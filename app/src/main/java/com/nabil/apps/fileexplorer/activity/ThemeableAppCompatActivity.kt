package com.nabil.apps.fileexplorer.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.R


abstract class ThemeableAppCompatActivity: AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var themeName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeName = PreferenceManager.getDefaultSharedPreferences(this).
                getString(getString(R.string.pref_key_theme), "green")!!
        setTheme(AppConstants.themeIds[themeName]?:R.style.AppThemeGreen)
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
//        findViewById<Toolbar>(R.id.toolbar)?.popupTheme =
//                (AppConstants.popupThemeIds[themeName]?:R.style.PopupThemeGreen)
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        recreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this).
                unregisterOnSharedPreferenceChangeListener(this)
    }
}