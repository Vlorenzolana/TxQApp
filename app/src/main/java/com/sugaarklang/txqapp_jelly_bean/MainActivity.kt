package com.sugaarklang.txqapp_jelly_bean

import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View               // For View.GONE and View.VISIBLE constants
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

class MainActivity : AppCompatActivity() {

    private lateinit var socketClient: SocketClient
    private lateinit var serverThread: SocketServerThread
    private lateinit var gridView: GridViewCanvas

    // Esta es la forma de crear variables estaticas en kotlin
    companion object {
        val port = 12345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val myIpTextView = findViewById<TextView>(R.id.myIpTextView)
        val ipInput = findViewById<EditText>(R.id.ipInput)
        val connectButton = findViewById<Button>(R.id.connectButton)

        // Show local IP
        val myIp = getLocalIpAddress()
        myIpTextView.text = "Your IP: $myIp"


        // Setup grid view (your existing custom view)
        gridView = GridViewCanvas(this) { touchedFromRemote ->
            if (!touchedFromRemote) {
                socketClient.send("TOUCH")
            }
        }

        // Start server thread
        serverThread = SocketServerThread { message ->
            if (message == "TOUCH") {
                runOnUiThread {
                    gridView.blinkFromRemote()
                }
            }
        }
        serverThread.start()

        // Connect button: create client with entered IP
        connectButton.setOnClickListener {
            val targetIp = ipInput.text.toString().trim()
            if (targetIp.isNotEmpty()) {
                socketClient = SocketClient(targetIp)
                runOnUiThread {
                    transitionToGrid(ipInput)
                }
            }
        }

    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }

    private fun transitionToGrid(ipInput: EditText) {
        // Hide the keyboard
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ipInput.windowToken, 0)
        // Hide ActionBar if using AppCompat
        supportActionBar?.hide()

        // Enable immersive fullscreen mode
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        // Force fullscreen flags
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(gridView)

    }
}
