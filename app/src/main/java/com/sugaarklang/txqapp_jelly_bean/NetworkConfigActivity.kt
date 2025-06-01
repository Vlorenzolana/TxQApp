package com.sugaarklang.txqapp_jelly_bean

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class NetworkConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network_config)

        val myIpText = findViewById<TextView>(R.id.myIpText)
        val peerIpInput = findViewById<EditText>(R.id.peerIpInput)
        val intervalInput = findViewById<EditText>(R.id.intervalInput)
        val radioSender = findViewById<RadioButton>(R.id.radioSender)
        val radioReceiver = findViewById<RadioButton>(R.id.radioReceiver)
        val btnStart = findViewById<Button>(R.id.btnStart)

        val myIp = getLocalIpAddress()
        myIpText.text = myIp
        peerIpInput.setText(suggestPeerIp(myIp))

        btnStart.setOnClickListener {
            val intent = Intent(this, NetworkActivity::class.java)
            intent.putExtra("peerIp", peerIpInput.text.toString().trim())
            intent.putExtra("intervalMs", intervalInput.text.toString().toLongOrNull() ?: 2000)
            intent.putExtra("isSender", radioSender.isChecked)
            startActivity(intent)
        }
    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        return if (wifiManager != null && wifiManager.connectionInfo != null)
            Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        else
            "0.0.0.0"
    }

    private fun suggestPeerIp(myIp: String): String {
        val base = myIp.substringBeforeLast('.')
        val last = myIp.substringAfterLast('.').toInt()
        return base + "." + if (last == 100) "101" else "100"
    }
}