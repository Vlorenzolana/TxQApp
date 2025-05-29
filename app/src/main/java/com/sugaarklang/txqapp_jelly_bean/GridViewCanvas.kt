package com.sugaarklang.txqapp_jelly_bean

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.util.Log

class GridViewCanvas(context: Context, val onTouchCallback: (Boolean) -> Unit) : View(context) {
    private val fillPaint = Paint().apply { color = Color.BLACK }
    private val blinkPaint = Paint().apply { color = Color.WHITE }

    private var flashWholeScreen = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (flashWholeScreen) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), blinkPaint)
        } else {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            flashVisualPattern()
            onTouchCallback(false)
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun flashVisualPattern() {
        flashWholeScreen = true
        invalidate()
        handler.postDelayed({
            flashWholeScreen = false
            invalidate()
        }, 80)
    }

    fun flashFullScreen() {
        flashVisualPattern()
    }
}