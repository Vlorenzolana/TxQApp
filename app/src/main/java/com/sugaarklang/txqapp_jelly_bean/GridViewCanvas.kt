package com.sugaarklang.txqapp_jelly_bean

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GridViewCanvas(context: Context, val onTouchCallback: (Boolean) -> Unit) : View(context) {

    private val fillPaint = Paint().apply { color = Color.BLACK }
    private val blinkPaint = Paint().apply { color = Color.WHITE }

    private var flashWholeScreen = false
    private val handler = Handler(Looper.getMainLooper())

    private val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.beep)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(),
            if (flashWholeScreen) blinkPaint else fillPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            flashAndPlay()
            onTouchCallback(false)
            return true
        }
        return super.onTouchEvent(event)
    }

    fun blinkFromRemote() {
        flashAndPlay()
    }

    private fun flashAndPlay() {
        flashWholeScreen = true
        invalidate()

        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }

            val duration = mediaPlayer.duration
            val maxStart = duration - 100
            if (maxStart > 0) {
                val randomStart = Random.nextInt(maxStart)
                mediaPlayer.seekTo(randomStart)
            } else {
                mediaPlayer.seekTo(0)
            }

            mediaPlayer.start()

            // Detener después de 100ms
            handler.postDelayed({
                mediaPlayer.pause()
                flashWholeScreen = false
                invalidate()
            }, 100)

        } catch (e: Exception) {
            Log.e("AUDIO", "Error reproduciendo sonido: ${e.message}")
        }
    }
}