package com.sugaarklang.txqapp_jelly_bean

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.View
import kotlin.random.Random

class DemoVisualView(context: Context) : View(context) {

    private var flashActive = false
    private var circleVisible = false
    private var circleX = 0f
    private var circleY = 0f

    private val flashPaint = Paint().apply {
        color = Color.WHITE
    }

    private val circlePaint = Paint().apply {
        color = Color.argb(100, 255, 255, 255) // pequeño círculo blanco translúcido
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

    fun randomTouch(width: Int, height: Int) {
        val x = Random.nextFloat() * width
        val y = Random.nextFloat() * height
        showTouchCircle(x, y)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (flashActive) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flashPaint)
        }
        if (circleVisible) {
            canvas.drawCircle(circleX, circleY, 20f, circlePaint) // más pequeño
        }
    }
}