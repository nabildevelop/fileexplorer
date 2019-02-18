package com.nabil.apps.fileexplorer.activity


import android.os.Bundle
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity: ThemeableAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(R.id.container, SettingsFragment()).commit()
    }
}