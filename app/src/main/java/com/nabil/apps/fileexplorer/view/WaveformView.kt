package com.nabil.apps.fileexplorer.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*

class WaveformView(context: Context, attrs: AttributeSet): View(context, attrs) {

    var renderer: WaveformRenderer? = null
    var waveform: ByteArray? = null
        set(value) {
            field = value?.copyOf()
            invalidate()
        }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderer?.render(canvas, width, height, waveform)
    }
}