
package com.sugaarklang.txqapp_jelly_bean

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class NetworkActivity : AppCompatActivity() {

    private lateinit var socket: SocketHandler
    private lateinit var gridView: GridViewCanvas
    private lateinit var visualView: TouchVisualFeedback
    private lateinit var role: String
    private lateinit var remoteIp: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        role = intent.getStringExtra("ROLE") ?: "A"
        remoteIp = intent.getStringExtra("IP") ?: "192.168.1.2"

        visualView = TouchVisualFeedback(this)
        gridView = GridViewCanvas(
            this,
            if (role == "A") R.raw.beep_a else R.raw.beep_b,
            onTouchLocal = { offset ->
                visualView.randomTouch()
                socket.sendMessage(remoteIp, "TOUCH", offset.toString())
            },
            onTouchRemote = { offset ->
                visualView.randomTouch()
                gridView.blinkFromRemote(offset)
            }
        )

        val frame = FrameLayout(this).apply {
            setBackgroundColor(0xFF000000.toInt())
            addView(gridView)
            addView(visualView)
        }

        setContentView(frame)

        socket = SocketHandler(MainActivity.port)
        socket.startServer { event, data ->
            if (event == "TOUCH") {
                val offset = data.toIntOrNull()
                runOnUiThread {
                    visualView.randomTouch()
                    gridView.blinkFromRemote(offset)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.shutdown()
    }
}
