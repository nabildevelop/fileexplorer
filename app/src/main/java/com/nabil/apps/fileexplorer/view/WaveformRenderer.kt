package com.nabil.apps.fileexplorer.view

import android.graphics.Canvas

interface WaveformRenderer {
    fun render(canvas: Canvas, width: Int, height: Int, waveForm: ByteArray?)
}