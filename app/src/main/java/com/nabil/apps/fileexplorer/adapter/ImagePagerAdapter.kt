package com.nabil.apps.fileexplorer.adapter

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.fragment.ImageFragment
import java.io.File

class ImagePagerAdapter(fm: FragmentManager, var data: List<File>): FragmentPagerAdapter(fm) {
    var currentFragment: ImageFragment?=null
    override fun getItem(position: Int): Fragment {
        return ImageFragment().apply{arguments= Bundle().apply{putString(AppConstants.EXTRA_FILE_PATH, data[position].path)}}
    }
    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        if(currentFragment!=item)
            currentFragment = item as ImageFragment
        super.setPrimaryItem(container, position, item)
    }
    override fun getCount() = data.size
}