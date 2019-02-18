package com.nabil.apps.fileexplorer.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.annotation.ColorInt

class SimpleWaveFormRenderer private constructor(@ColorInt val backgroundColor: Int
                                                 , val foregroundPaint: Paint, val waveformPath: Path): WaveformRenderer{
    companion object {
        private const val Y_FACTOR: Int = 0xFF


        fun newInstance(@ColorInt backgroundColor: Int, @ColorInt foregroundColor: Int):WaveformRenderer{
            val paint = Paint()
            paint.color = foregroundColor
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            val waveformPath = Path()
            return SimpleWaveFormRenderer(backgroundColor, paint, waveformPath)
        }
    }

    override fun render(canvas: Canvas, width: Int, height: Int, waveForm: ByteArray?) {
        canvas.drawColor(backgroundColor)
        waveformPath.reset()
        waveForm?.let{renderWaveform(it, width.toFloat(), height.toFloat())}?:run{renderBlank(width.toFloat(), height.toFloat())}
        canvas.drawPath(waveformPath, foregroundPaint)
    }

    private fun renderWaveform(waveform: ByteArray, width: Float, height: Float){
        val xIncrement: Float = width / waveform.size
        val yIncrement: Float = height / Y_FACTOR
        //val halfHeight = (height / 2).toInt()
        for(i in 0 until waveform.size){
            waveformPath.moveTo(xIncrement*i, height)
            val yPosition = if(waveform[i]>0) height-(yIncrement*waveform[i]) else -(yIncrement*waveform[i])
            waveformPath.lineTo(xIncrement*i, yPosition)
        }
        //waveformPath.lineTo(width, halfHeight.toFloat())
    }

    private fun renderBlank(width: Float, height: Float){
        val y = height/ 2
        waveformPath.moveTo(0f, y)
        waveformPath.lineTo(width, y)
    }
}