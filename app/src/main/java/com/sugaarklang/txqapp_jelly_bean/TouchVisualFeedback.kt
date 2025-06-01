package com.sugaarklang.txqapp_jelly_bean

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.View
import kotlin.random.Random

class TouchVisualFeedback(context: Context) : View(context) {

    private var flashActive = false
    private var circleVisible = false
    private var circleX = 0f
    private var circleY = 0f

    private val flashPaint = Paint().apply {
        color = Color.WHITE
    }

    private val circlePaint = Paint().apply {
        color = Color.argb(180, 173, 255, 47) // lima fluorescente semitransparente
        isAntiAlias = true
    }

    private val handler = Handler(Looper.getMainLooper())

    fun flash(duration: Long = 100L) {
        flashActive = true
        invalidate()
        handler.postDelayed({
            flashActive = false
            invalidate()
        }, duration)
    }

    fun showTouchCircle(x: Float, y: Float, duration: Long = 200L) {
        circleX = x
        circleY = y
        circleVisible = true
        invalidate()
        handler.postDelayed({
            circleVisible = false
            invalidate()
        }, duration)
    }

    fun randomTouch(duration: Long = 200L) {
        val x = Random.nextFloat() * (width.takeIf { it > 0 } ?: 1080)
        val y = Random.nextFloat() * (height.takeIf { it > 0 } ?: 1920)
        showTouchCircle(x, y, duration)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (flashActive) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flashPaint)
        }
        if (circleVisible) {
            canvas.drawCircle(circleX, circleY, 50f, circlePaint)
        }
    }
}
