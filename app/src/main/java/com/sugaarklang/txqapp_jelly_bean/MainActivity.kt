package com.sugaarklang.txqapp_jelly_bean

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDemo = findViewById<Button>(R.id.btnDemoMode)
        btnDemo.setOnClickListener {
            startActivity(Intent(this, DemoActivity::class.java))
        }

        val btnNetwork = findViewById<Button>(R.id.btnNetworkMode)
        btnNetwork.setOnClickListener {
            startActivity(Intent(this, NetworkConfigActivity::class.java))
        }
    }

    companion object {
        const val port = 12345
    }
}