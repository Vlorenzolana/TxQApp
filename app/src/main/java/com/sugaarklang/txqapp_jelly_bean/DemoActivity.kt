package com.sugaarklang.txqapp_jelly_bean

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class DemoActivity : AppCompatActivity() {

    private val compases = listOf(
        4f, 4f, 3f, 3f, 2f, 2f, 2f, 2f,
        1f, 1f, 1f, 1f,
        0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
        2f, 2f, 2f, 2f, 4f, 4f
    )

    private val handler = Handler(Looper.getMainLooper())
    private var currentIndex = 0

    private lateinit var mediaPlayerA: MediaPlayer
    private lateinit var mediaPlayerB: MediaPlayer
    private lateinit var visualView: DemoVisualView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        visualView = DemoVisualView(this)
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(0xFF000000.toInt()) // fondo negro
            addView(visualView)
        })

        mediaPlayerA = MediaPlayer.create(this, R.raw.beep_a)
        mediaPlayerB = MediaPlayer.create(this, R.raw.beep_b)

        startLoop()
    }

    private fun startLoop() {
        if (currentIndex >= compases.size) return

        val durationInSeconds = compases[currentIndex]
        val durationInMs = (durationInSeconds * 1000).toLong()
        val isA = currentIndex % 2 == 0
        val player = if (isA) mediaPlayerA else mediaPlayerB

        Log.d("RHYTHM", "Compás ${currentIndex / 2 + 1} ${if (isA) "A" else "B"} duración $durationInSeconds s")

        visualView.randomTouch(visualView.width.takeIf { it > 0 } ?: 1080, visualView.height.takeIf { it > 0 } ?: 1920)

        handler.postDelayed({
            visualView.flash()
            player.seekTo(0)
            player.start()
        }, 100)

        handler.postDelayed({
            currentIndex++
            startLoop()
        }, durationInMs)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayerA.release()
        mediaPlayerB.release()
    }
}
