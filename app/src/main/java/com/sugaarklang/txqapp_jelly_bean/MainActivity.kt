package com.sugaarklang.txqapp_jelly_bean

import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

class MainActivity : AppCompatActivity() {

    private lateinit var socketClient: SocketClient
    private lateinit var serverThread: SocketServerThread
    private lateinit var gridView: GridViewCanvas

    companion object {
        val port = 12345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myIpTextView = findViewById<TextView>(R.id.myIpTextView)
        val ipInput = findViewById<EditText>(R.id.ipInput)
        val connectButton = findViewById<Button>(R.id.connectButton)

        val demoMode = true // Activa modo demo simulado

        val myIp = getLocalIpAddress()
        myIpTextView.text = "Your IP: $myIp"

        connectButton.setOnClickListener {
            socketClient = SocketClient("127.0.0.1") // no se usa en demo

            gridView = GridViewCanvas(this,
                onTouchLocal = { offset ->
                    if (!demoMode) socketClient.send("TOUCH", offset.toString())
                    if (demoMode) {
                        gridView.blinkFromRemote(offset + 250) // simula respuesta
                    }
                },
                onTouchRemote = { offset ->
                    gridView.blinkFromRemote(offset)
                }
            )

            transitionToGrid(ipInput)
            if (!demoMode) startServer()
        }
    }

    private fun startServer() {
        serverThread = SocketServerThread { event, data ->
            if (event == "TOUCH") {
                val offset = data.toIntOrNull()
                runOnUiThread {
                    gridView.blinkFromRemote(offset)
                }
            }
        }
        serverThread.start()
    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }

    private fun transitionToGrid(ipInput: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ipInput.windowToken, 0)
        supportActionBar?.hide()

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(gridView)
    }
}