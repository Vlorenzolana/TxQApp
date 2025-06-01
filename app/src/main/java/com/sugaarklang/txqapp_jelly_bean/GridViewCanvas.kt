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
import androidx.annotation.RawRes
import kotlin.math.abs
import kotlin.random.Random

class GridViewCanvas(
    context: Context,
    @RawRes soundResId: Int,
    val onTouchLocal: (Int) -> Unit,
    val onTouchRemote: (Int?) -> Unit
) : View(context) {

    private val fillPaint = Paint().apply { color = Color.BLACK }
    private val blinkPaint = Paint().apply { color = Color.WHITE }
    private var isLocked = false
    private var flashWholeScreen = false
    private val handler = Handler(Looper.getMainLooper())

    private val mediaPlayer: MediaPlayer = MediaPlayer.create(context, soundResId)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(),
            if (flashWholeScreen) blinkPaint else fillPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val offset = playRandomSnippet()
            onTouchLocal(offset)
            flash()
            return true
        }
        return super.onTouchEvent(event)
    }

    fun blinkFromRemote(offsetMs: Int?) {
        if (isLocked) return
        isLocked = true

        val offset = offsetMs ?: 0
        playDifferentSnippet(offset)
        flash()

        handler.postDelayed({
            isLocked = false
        }, 200)
    }

    private fun flash() {
        flashWholeScreen = true
        invalidate()
        handler.postDelayed({
            flashWholeScreen = false
            invalidate()
        }, 100)
    }

    private fun playRandomSnippet(): Int {
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
            val duration = mediaPlayer.duration
            val maxStart = duration - 100
            val randomStart = if (maxStart > 0) Random.nextInt(maxStart) else 0
            mediaPlayer.seekTo(randomStart)
            mediaPlayer.start()
            handler.postDelayed({ mediaPlayer.pause() }, 100)
            return randomStart
        } catch (e: Exception) {
            Log.e("AUDIO", "Error reproducir aleatorio: ${e.message}")
            return 0
        }
    }

    private fun playDifferentSnippet(referenceOffset: Int) {
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
            val duration = mediaPlayer.duration
            val maxStart = duration - 100
            val range = (0..maxStart).filter { abs(it - referenceOffset) > 200 }
            val newOffset = if (range.isNotEmpty()) range.random() else 0
            mediaPlayer.seekTo(newOffset)
            mediaPlayer.start()
            handler.postDelayed({ mediaPlayer.pause() }, 100)
        } catch (e: Exception) {
            Log.e("AUDIO", "Error reproducir diferente: ${e.message}")
        }
    }
}
