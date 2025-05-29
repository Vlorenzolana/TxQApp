package com.sugaarklang.txqapp_jelly_bean

import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var socketClient: SocketClient
    private lateinit var serverThread: SocketServerThread
    private lateinit var gridView: GridViewCanvas
    private lateinit var broadcastSender: BroadcastSender
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myIpTextView = findViewById<TextView>(R.id.myIpTextView)
        val myIp = getLocalIpAddress()
        myIpTextView.text = "Your IP: $myIp"

        // Start grid
        gridView = GridViewCanvas(this) { touchedFromRemote ->
            if (!touchedFromRemote) {
                socketClient.send("TOUCH")
            }
        }

        // Start server thread
        serverThread = SocketServerThread { message ->
            if (message == "TOUCH") {
                runOnUiThread {
                    gridView.flashFullScreen()
                }
            }
        }
        serverThread.start()

        // Start broadcasting our IP
        broadcastSender = BroadcastSender(myIp)
        broadcastSender.start()

        // Listen for other broadcasts
        broadcastReceiver = BroadcastReceiver({ otherIp ->
            if (!::socketClient.isInitialized) {
                socketClient = SocketClient(otherIp)
                socketClient.connect {
                    runOnUiThread {
                        transitionToGrid()
                    }
                }
            }
        })
        broadcastReceiver.start()
    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }

    private fun transitionToGrid() {
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(gridView)
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastSender.stopBroadcast()
        broadcastReceiver.stopReceiver()
        serverThread.stopServer()
    }
}