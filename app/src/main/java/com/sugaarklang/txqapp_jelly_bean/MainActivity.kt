package com.sugaarklang.txqapp_jelly_bean

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnDemoMode).setOnClickListener {
            val intent = Intent(this, DemoActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnNetworkMode).setOnClickListener {
            // Aquí se lanzaría otra Activity futura para modo en red
        }
    }
}
