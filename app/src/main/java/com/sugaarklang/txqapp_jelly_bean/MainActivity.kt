package com.sugaarklang.txqapp_jelly_bean

import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var ipDiscovery: IpDiscovery
    private var ipDiscovered: String? = null

    companion object {
        val port = 12345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myIpTextView = findViewById<TextView>(R.id.myIpTextView)
        val ipInput = findViewById<EditText>(R.id.ipInput)
        val connectButton = findViewById<Button>(R.id.connectButton)

        // Mostrar IP local
        val myIp = getLocalIpAddress()
        myIpTextView.text = "Your IP: $myIp"

        // Inicialmente desactivado hasta tener IP válida (manual o auto)
        connectButton.isEnabled = false

        // Descubrimiento automático de IPs (anuncia y escucha)
        ipDiscovery = IpDiscovery(
            onIpDiscovered = { senderIp ->
                runOnUiThread {
                    if (senderIp != myIp) {
                        ipDiscovered = senderIp
                        ipInput.setText(senderIp)
                        connectButton.text = "Connect to $senderIp"
                        connectButton.isEnabled = true
                    }
                }
            }
        )
        ipDiscovery.startListening()
        ipDiscovery.startBroadcasting()

        // Permite escribir manualmente si el usuario lo desea
        ipInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                connectButton.isEnabled = s.toString().trim().isNotEmpty()
                if (s.toString().trim() != ipDiscovered) {
                    connectButton.text = "Connect"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Al pulsar conectar: detiene broadcast/escucha y monta la GridView
        connectButton.setOnClickListener {
            val targetIp = ipInput.text.toString().trim()
            if (targetIp.isNotEmpty()) {
                ipDiscovery.stop()
                socketClient = SocketClient(targetIp)

                gridView = GridViewCanvas(this,
                    onTouchLocal = { offset ->
                        socketClient.send("TOUCH", offset.toString())
                    },
                    onTouchRemote = { offset ->
                        gridView.blinkFromRemote(offset)
                    }
                )

                transitionToGrid(ipInput)
                startServer()
            }
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
