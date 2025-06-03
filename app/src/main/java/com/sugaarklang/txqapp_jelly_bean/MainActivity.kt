package com.sugaarklang.txqapp_jelly_bean

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log // Importación añadida para Logcat

class MainActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var rootView: FrameLayout
    private lateinit var panelConfig: LinearLayout
    private lateinit var panelPerformance: FrameLayout
    private lateinit var txtIp: TextView
    private lateinit var editIp: EditText
    private lateinit var btnConnectIp: Button
    private lateinit var txtStatus: TextView // Used for network and rhythm engine status
    private lateinit var btnStart: Button
    private lateinit var txtPerformance: TextView // Used for "YOUR TURN" / "WAITING..."

    // Application Logic Components
    private lateinit var networkManager: NetworkManager
    private lateinit var rhythm: TxalapartaEngine

    // State Variables
    private var isMyTurn = false // True if it's this device's turn to play a rhythm sequence.
    private var amIServer = false // True if this device is acting as the server (session initiator).

    // Audio Components
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0
    private var soundLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        rootView = findViewById(R.id.root_view)
        panelConfig = findViewById(R.id.panel_config)
        panelPerformance = findViewById(R.id.panel_performance)
        txtIp = findViewById(R.id.txt_ip)
        editIp = findViewById(R.id.edit_ip)
        btnConnectIp = findViewById(R.id.btn_connect_ip)
        txtStatus = findViewById(R.id.txt_status)
        btnStart = findViewById(R.id.btn_start)
        txtPerformance = findViewById(R.id.txt_performance)

        // Display local IP address for easy peer connection
        txtIp.text = "Your IP: ${NetworkManager.getLocalIpAddress()}"

        // Set up the sound pool for Txalaparta hits
        setupSound()

        // Initialize NetworkManager with a listener to handle network events
        networkManager = NetworkManager(this, object : NetworkManager.Listener {
            override fun onTurn() {
                // When a "TURN" message is received from the peer, this device performs its turn.
                Log.d("MainActivity", "NetworkManager.onTurn() called. Performing rhythm turn.")
                rhythm.performTurn()
            }
            override fun onStatusUpdate(status: String) {
                // Update UI with network status messages.
                runOnUiThread { txtStatus.text = status }
                Log.d("MainActivity", "Network Status: $status")
            }
            override fun onConnected() {
                // Callback when a connection is successfully established.
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connection established!", Toast.LENGTH_SHORT).show()
                    // If this device is the server and just connected, it can initiate the rhythm.
                    if (amIServer) {
                        Log.d("MainActivity", "Server connected, resetting rhythm and performing first turn.")
                        rhythm.reset() // Reset rhythm before starting a new performance.
                        rhythm.performTurn()
                    } else {
                        Log.d("MainActivity", "Client connected. Waiting for server to initiate turn.")
                        // Client waits for the server to send the first "TURN" message.
                    }
                }
            }
        })

        // Initialize TxalapartaEngine with a listener to handle rhythm events
        rhythm = TxalapartaEngine(object : TxalapartaEngine.Listener {
            override fun onHit() {
                // When a hit occurs in the rhythm engine, trigger visual feedback and play sound.
                triggerHitVisual()
                playSound()
            }
            override fun onTurnSwitch(isMyTurnNow: Boolean) {
                // Update turn status and UI.
                isMyTurn = isMyTurnNow
                runOnUiThread {
                    showPerformance() // Ensure performance panel is visible.
                    txtPerformance.text = if (isMyTurn) "YOUR TURN" else "WAITING…"
                }

                // If my turn has just ended AND a network connection exists,
                // send a "TURN" message to the peer.
                if (!isMyTurnNow && networkManager.socket != null) {
                    Log.d("MainActivity", "My turn ended, socket is active. Sending 'TURN' to peer.")
                    networkManager.sendHit()
                } else if (!isMyTurnNow && networkManager.socket == null) {
                    Log.w("MainActivity", "My turn ended, but no active socket to send 'TURN'.")
                }
            }
            override fun onEngineStatusUpdate(status: String) {
                // Update UI with Txalaparta engine status (phase, tempo, density).
                // This is displayed in txtStatus, which is also used for network status.
                // In a more complex application, a dedicated TextView would be used.
                runOnUiThread {
                    txtStatus.text = status
                }
                Log.d("MainActivity", "Engine Status: $status")
            }
        })

        // Set up listener for the "CONNECT" button
        btnConnectIp.setOnClickListener {
            val peerIp = editIp.text.toString().trim()
            if (peerIp.isNotEmpty()) {
                // This device will act as the client.
                amIServer = false
                Log.d("MainActivity", "CONNECT button clicked. Attempting to connect as client to $peerIp.")
                networkManager.initiateConnection(peerIp) // Explicitly try to connect as client.
                txtStatus.text = "Connecting to $peerIp…"
            } else {
                Toast.makeText(this, "Enter peer IP", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up listener for the "START PERFORMANCE" button
        btnStart.setOnClickListener {
            // This device will act as the server (performance initiator).
            amIServer = true
            rhythm.reset() // Reset rhythm state to soft start.
            txtPerformance.text = "" // Clear performance text initially.
            showPerformance() // Switch to performance panel.

            // If not already connected, start the server.
            if (networkManager.socket == null) {
                Log.d("MainActivity", "START PERFORMANCE clicked. No active socket, starting server.")
                networkManager.startServer() // Explicitly start listening as server.
                txtStatus.text = "Starting server and waiting for peer..."
                // The rhythm will be initiated by the onConnected() callback once a peer connects.
            } else {
                // If already connected (e.g., peer connected to us, or we connected to them earlier),
                // and this device is the server, initiate the rhythm immediately.
                Log.d("MainActivity", "START PERFORMANCE clicked. Socket active, performing first turn as server.")
                rhythm.performTurn()
            }
        }
    }

    // Loads the Txalaparta sound into SoundPool.
    private fun setupSound() {
        // SoundPool constructor parameters: maxStreams, streamType, srcQuality.
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        // Load the sound resource (R.raw.txalaparta).
        soundId = soundPool.load(this, R.raw.txalaparta, 1)
        // Set a listener to know when the sound has finished loading.
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            soundLoaded = true
            Log.d("MainActivity", "Sound loaded: $soundLoaded")
        }
    }

    // Plays the loaded Txalaparta sound.
    private fun playSound() {
        if (soundLoaded) {
            // Play sound: soundID, leftVolume, rightVolume, priority, loop, rate.
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            Log.d("MainActivity", "Playing sound.")
        } else {
            Log.w("MainActivity", "Sound not loaded, cannot play.")
        }
    }

    // Triggers a brief visual flash on the performance panel.
    private fun triggerHitVisual() {
        runOnUiThread {
            // Temporarily change background to white, then back to black.
            panelPerformance.setBackgroundColor(Color.WHITE)
            // Use Handler to delay the color change back to black.
            Handler(Looper.getMainLooper()).postDelayed({
                panelPerformance.setBackgroundColor(Color.BLACK)
            }, 120) // Flash duration in milliseconds.
        }
    }

    // Switches the UI from configuration panel to performance panel.
    private fun showPerformance() {
        runOnUiThread {
            panelConfig.visibility = View.GONE
            panelPerformance.visibility = View.VISIBLE
        }
    }

    // Lifecycle method: release SoundPool resources when activity is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called. Releasing resources.")
        soundPool.release() // Release SoundPool.
        networkManager.closeConnection() // Ensure network connection is closed.
    }
}
