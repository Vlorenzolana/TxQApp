package com.sugaarklang.txqapp_jelly_bean

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var rootView: View
    private lateinit var txtStatus: TextView
    private lateinit var txtIp: TextView
    private lateinit var editIp: EditText
    private lateinit var btnConnectIp: Button
    private lateinit var spinnerVariant: Spinner
    private lateinit var spinnerCurve: Spinner
    private lateinit var seekDensity: SeekBar
    private lateinit var seekTempo: SeekBar
    private lateinit var btnStart: Button

    private lateinit var networkManager: NetworkManager
    private lateinit var rhythm: TxalapartaEngine

    private var isMyTurn = false
    private var amIServer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootView = findViewById(R.id.root_view)
        txtStatus = findViewById(R.id.txt_status)
        txtIp = findViewById(R.id.txt_ip)
        editIp = findViewById(R.id.edit_ip)
        btnConnectIp = findViewById(R.id.btn_connect_ip)
        spinnerVariant = findViewById(R.id.spinner_variant)
        spinnerCurve = findViewById(R.id.spinner_curve)
        seekDensity = findViewById(R.id.seek_density)
        seekTempo = findViewById(R.id.seek_tempo)
        btnStart = findViewById(R.id.btn_start)

        txtIp.text = "Local IP: ${NetworkManager.getLocalIpAddress()}"

        spinnerVariant.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, TxalapartaEngine.VARIANTS)
        spinnerCurve.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, TxalapartaEngine.CURVES)
        seekDensity.max = 100; seekDensity.progress = 30
        seekTempo.max = 100; seekTempo.progress = 30

        networkManager = NetworkManager(this, object : NetworkManager.Listener {
            override fun onTurn() {
                rhythm.performTurn()
            }
            override fun onStatusUpdate(status: String) {
                runOnUiThread { txtStatus.text = status }
            }
        })

        rhythm = TxalapartaEngine(object : TxalapartaEngine.Listener {
            override fun onHit() {
                triggerHitVisual()
                // NO mandes sendHit() aquí, solo visual
            }
            override fun onTurnSwitch(isMyTurnNow: Boolean) {
                isMyTurn = isMyTurnNow
                runOnUiThread { txtStatus.text = if (isMyTurn) "YOUR TURN" else "WAITING…" }
                if (!isMyTurnNow) {
                    // Cuando termina mi turno, aviso al peer:
                    sendTurnToPeer()
                }
            }
        })

        btnConnectIp.setOnClickListener {
            val peerIp = editIp.text.toString().trim()
            if (peerIp.isNotEmpty()) {
                amIServer = false
                networkManager.connectToPeer(peerIp)
                txtStatus.text = "Connecting to $peerIp…"
            } else {
                Toast.makeText(this, "Enter peer IP", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            val variant = spinnerVariant.selectedItem.toString()
            val curve = spinnerCurve.selectedItem.toString()
            val density = seekDensity.progress / 100.0f
            val tempo = seekTempo.progress + 30
            rhythm.configure(variant, curve, density, tempo)
            rhythm.reset()
            amIServer = true
            // SOLO la tablet “servidora” inicia la alternancia:
            rhythm.performTurn()
        }
    }

    private fun sendTurnToPeer() {
        networkManager.sendHit()
    }

    private fun triggerHitVisual() {
        runOnUiThread {
            val flashColor = Color.YELLOW
            val original = Color.WHITE
            val anim = ValueAnimator.ofObject(ArgbEvaluator(), original, flashColor, original)
            anim.duration = 350
            anim.addUpdateListener { rootView.setBackgroundColor(it.animatedValue as Int) }
            anim.start()
        }
    }
}
