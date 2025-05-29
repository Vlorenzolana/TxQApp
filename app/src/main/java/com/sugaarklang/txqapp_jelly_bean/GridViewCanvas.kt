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
import kotlin.random.Random

//class GridViewCanvas(context: Context, val onTouchCallback: (Boolean) -> Unit, val onInactivity: Runnable) : View(context) {
class GridViewCanvas(context: Context, val onTouchCallback: (Boolean) -> Unit) : View(context) {
//  private val INACTIVITY_TIMEOUT_MS = 1_000L  // 10 seconds, change as needed
    private val rows = 4
    private val cols = 4

    private val fillPaint = Paint().apply {
        color = Color.BLACK
    }
    private val blinkPaint = Paint().apply {
        color = Color.WHITE
    }

    private var cellWidth = 0f
    private var cellHeight = 0f
    private var blinkRow = -1
    private var blinkCol = -1

    private val handler = Handler(Looper.getMainLooper())

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        cellWidth = width / cols.toFloat()
        cellHeight = height / rows.toFloat()

        // Fill background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)

        // Draw blinked cell if any
        if (blinkRow != -1 && blinkCol != -1) {
            val left = blinkCol * cellWidth
            val top = blinkRow * cellHeight
            val right = left + cellWidth
            val bottom = top + cellHeight
            canvas.drawRect(left, top, right, bottom, blinkPaint)
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellWidth).toInt()
            val row = (event.y / cellHeight).toInt()

            blinkRow = row
            blinkCol = col
            invalidate()

            onTouchCallback(false) // local touch

            handler.postDelayed({
                blinkRow = -1
                blinkCol = -1
                invalidate()

            }, 100)
            Log.d("GRID", "On Touch Event")
            return true
        }
        return super.onTouchEvent(event)
    }

    fun blinkFromRemote() {
        // blink full screen for simplicity
        blinkRow = Random.nextInt(this.rows)
        blinkCol = Random.nextInt(this.cols)
        invalidate()
        handler.postDelayed({
            blinkRow = -1
            blinkCol = -1
            invalidate()
        }, 100)
    }
}