package com.nabil.apps.fileexplorer.view


import android.view.ScaleGestureDetector

import android.graphics.Bitmap
import android.view.MotionEvent

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView


// Credit to Nicolas Tyler
// https://stackoverflow.com/a/17649895/5895352


class ZoomableImageView(internal var context: Context, attr: AttributeSet) : ImageView(context, attr) {
    internal var matrix = Matrix()
    internal var mode = NONE

    internal var last = PointF()
    internal var start = PointF()
    internal var minScale = 1f
    internal var maxScale = 4f
    internal var m: FloatArray

    internal var redundantXSpace: Float = 0.toFloat()
    internal var redundantYSpace: Float = 0.toFloat()
    internal var width: Float = 0.toFloat()
    internal var height: Float = 0.toFloat()
    internal var saveScale = 1f
    internal var right: Float = 0.toFloat()
    internal var bottom: Float = 0.toFloat()
    internal var origWidth: Float = 0.toFloat()
    internal var origHeight: Float = 0.toFloat()
    internal var bmWidth: Float = 0.toFloat()
    internal var bmHeight: Float = 0.toFloat()

    internal var mScaleDetector: ScaleGestureDetector

    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        matrix.setTranslate(1f, 1f)
        m = FloatArray(9)
        setImageMatrix(matrix)
        setScaleType(ScaleType.MATRIX)

        setOnTouchListener { v, event ->
            mScaleDetector.onTouchEvent(event)

            matrix.getValues(m)
            val x = m[Matrix.MTRANS_X]
            val y = m[Matrix.MTRANS_Y]
            val curr = PointF(event.x, event.y)

            when (event.action) {
                //when one finger is touching
                //set the mode to DRAG
                MotionEvent.ACTION_DOWN -> {
                    last.set(event.x, event.y)
                    start.set(last)
                    mode = DRAG
                }
                //when two fingers are touching
                //set the mode to ZOOM
                MotionEvent.ACTION_POINTER_DOWN -> {
                    last.set(event.x, event.y)
                    start.set(last)
                    mode = ZOOM
                }
                //when a finger moves
                //If mode is applicable move image
                MotionEvent.ACTION_MOVE ->
                    //if the mode is ZOOM or
                    //if the mode is DRAG and already zoomed
                    if (mode == ZOOM || mode == DRAG && saveScale > minScale) {
                        var deltaX = curr.x - last.x// x difference
                        var deltaY = curr.y - last.y// y difference
                        val scaleWidth = Math.round(origWidth * saveScale).toFloat()// width after applying current scale
                        val scaleHeight = Math.round(origHeight * saveScale).toFloat()// height after applying current scale
                        //if scaleWidth is smaller than the views width
                        //in other words if the image width fits in the view
                        //limit left and right movement
                        if (scaleWidth < width) {
                            deltaX = 0f
                            if (y + deltaY > 0)
                                deltaY = -y
                            else if (y + deltaY < -bottom)
                                deltaY = -(y + bottom)
                        } else if (scaleHeight < height) {
                            deltaY = 0f
                            if (x + deltaX > 0)
                                deltaX = -x
                            else if (x + deltaX < -right)
                                deltaX = -(x + right)
                        } else {
                            if (x + deltaX > 0)
                                deltaX = -x
                            else if (x + deltaX < -right)
                                deltaX = -(x + right)

                            if (y + deltaY > 0)
                                deltaY = -y
                            else if (y + deltaY < -bottom)
                                deltaY = -(y + bottom)
                        }//if the image doesnt fit in the width or height
                        //limit both up and down and left and right
                        //if scaleHeight is smaller than the views height
                        //in other words if the image height fits in the view
                        //limit up and down movement
                        //move the image with the matrix
                        matrix.postTranslate(deltaX, deltaY)
                        //set the last touch location to the current
                        last.set(curr.x, curr.y)
                    }
                //first finger is lifted
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK)
                        performClick()
                }
                // second finger is lifted
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            setImageMatrix(matrix)
            invalidate()
            true
        }
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        bmWidth = bm.width.toFloat()
        bmHeight = bm.height.toFloat()
    }

    fun setMaxZoom(x: Float) {
        maxScale = x
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }
            right = width * saveScale - width - 2f * redundantXSpace * saveScale
            bottom = height * saveScale - height - 2f * redundantYSpace * saveScale
            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2)
                if (mScaleFactor < 1) {
                    matrix.getValues(m)
                    val x = m[Matrix.MTRANS_X]
                    val y = m[Matrix.MTRANS_Y]
                    if (mScaleFactor < 1) {
                        if (Math.round(origWidth * saveScale) < width) {
                            if (y < -bottom)
                                matrix.postTranslate(0f, -(y + bottom))
                            else if (y > 0)
                                matrix.postTranslate(0f, -y)
                        } else {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0f)
                            else if (x > 0)
                                matrix.postTranslate(-x, 0f)
                        }
                    }
                }
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
                matrix.getValues(m)
                val x = m[Matrix.MTRANS_X]
                val y = m[Matrix.MTRANS_Y]
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0f)
                    else if (x > 0)
                        matrix.postTranslate(-x, 0f)
                    if (y < -bottom)
                        matrix.postTranslate(0f, -(y + bottom))
                    else if (y > 0)
                        matrix.postTranslate(0f, -y)
                }
            }
            return true
        }
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        //Fit to screen.
        val scale: Float
        val scaleX = width / bmWidth
        val scaleY = height / bmHeight
        scale = Math.min(scaleX, scaleY)
        matrix.setScale(scale, scale)
        setImageMatrix(matrix)
        saveScale = 1f

        // Center the image
        redundantYSpace = height - scale * bmHeight
        redundantXSpace = width - scale * bmWidth
        redundantYSpace /= 2f
        redundantXSpace /= 2f

        matrix.postTranslate(redundantXSpace, redundantYSpace)

        origWidth = width - 2 * redundantXSpace
        origHeight = height - 2 * redundantYSpace
        right = width * saveScale - width - 2f * redundantXSpace * saveScale
        bottom = height * saveScale - height - 2f * redundantYSpace * saveScale
        setImageMatrix(matrix)
    }

    companion object {

        internal val NONE = 0
        internal val DRAG = 1
        internal val ZOOM = 2
        internal val CLICK = 3
    }
}