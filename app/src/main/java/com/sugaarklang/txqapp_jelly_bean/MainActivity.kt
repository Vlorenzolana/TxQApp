package com.sugaarklang.txqapp_jelly_bean
import com.sugaarklang.txqapp_jelly_bean.TxalapartaEngine
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import java.util.Random
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var rootView: FrameLayout
    private lateinit var panelConfig: LinearLayout
    private lateinit var panelPerformance: FrameLayout
    private lateinit var txtIp: TextView
    private lateinit var editIp: EditText
    private lateinit var btnConnectIp: Button
    private lateinit var txtStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var txtPerformance: TextView

    // Application Logic Components
    private lateinit var networkManager: NetworkManager
    private lateinit var rhythm: TxalapartaEngine

    // State Variables
    private var isMyTurn = false
    private var amIServer = false

    // Audio Components
    private var mainMediaPlayer: MediaPlayer? = null // MediaPlayer para el sample principal de txalaparta
    private var isMainMediaPlayerPrepared = false

    // Nuevos MediaPlayers para los sonidos excepcionales
    private val exceptionalMediaPlayers: MutableList<MediaPlayer> = mutableListOf()
    private var exceptionalSoundsPreparedCount = 0
    private var totalExceptionalSoundsToLoad = 0

    private val random = Random() // Generador de números aleatorios
    private val handler = Handler(Looper.getMainLooper()) // Handler para pausar el sonido

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

        // Display local IP address
        txtIp.text = "Your IP: ${NetworkManager.getLocalIpAddress()}"

        // Set up all MediaPlayers
        setupMediaPlayers() // Renombrado de setupMediaPlayer() a setupMediaPlayers()

        // Initialize NetworkManager
        networkManager = NetworkManager(this, object : NetworkManager.Listener {
            override fun onTurn() {
                Log.e("MainActivity", "NetworkManager.onTurn() CALLED. Performing rhythm turn.")
                rhythm.performTurn()
            }
            override fun onStatusUpdate(status: String) {
                runOnUiThread { txtStatus.text = status }
                Log.d("MainActivity", "Network Status: $status")
            }
            override fun onConnected() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connection established!", Toast.LENGTH_SHORT).show()
                    if (amIServer) {
                        Log.e("MainActivity", "Server connected, RESETTING RHYTHM AND PERFORMING FIRST TURN.")
                        rhythm.reset() // Reinicia el ritmo y la fase a SOFT_START
                        rhythm.performTurn() // El servidor inicia el primer turno, lo que también iniciará el temporizador del ciclo
                    } else {
                        Log.e("MainActivity", "Client connected. WAITING FOR SERVER TO INITIATE TURN.")
                    }
                }
            }
        })

        // Initialize TxalapartaEngine
        rhythm = TxalapartaEngine(object : TxalapartaEngine.Listener {
            override fun onHit() {
                triggerHitVisual()
                playSound() // Llama a playSound que ahora usa el mainMediaPlayer

                // Lógica para reproducir un sonido excepcional de forma esporádica
                // Probabilidad del 3% (ajusta este valor para que sea más o menos frecuente)
                if (random.nextInt(100) < 3) {
                    playRandomExceptionalSnippet()
                }
            }
            override fun onTurnSwitch(isMyTurnNow: Boolean) {
                isMyTurn = isMyTurnNow
                runOnUiThread {
                    showPerformance()
                    txtPerformance.text = if (isMyTurn) "YOUR TURN" else "WAITING…"
                }
                // Solo enviar 'TURN' si no estamos en silencio y el socket está activo
                if (!isMyTurnNow && networkManager.socket != null && rhythm.currentPhase != TxalapartaEngine.PerformancePhase.SILENCE) {
                    Log.e("MainActivity", "My turn ended, SOCKET IS ACTIVE. SENDING 'TURN' to peer.")
                    networkManager.sendHit()
                } else if (!isMyTurnNow && networkManager.socket == null) {
                    Log.w("MainActivity", "My turn ended, but no active socket to send 'TURN'.")
                } else if (!isMyTurnNow && rhythm.currentPhase == TxalapartaEngine.PerformancePhase.SILENCE) {
                    Log.d("MainActivity", "My turn ended, but in SILENCE phase. Not sending 'TURN'.")
                }
            }
            override fun onEngineStatusUpdate(status: String) {
                runOnUiThread {
                    txtStatus.text = status
                }
                Log.d("MainActivity", "Engine Status: $status")
            }

            // --- Nuevos callbacks para el ciclo de pausa/reanudación ---
            override fun onPauseCycle() {
                runOnUiThread {
                    Log.e("MainActivity", "Entering PAUSE CYCLE (Silence).")
                    // Pausar todos los MediaPlayers
                    mainMediaPlayer?.pause()
                    for (mp in exceptionalMediaPlayers) {
                        mp.pause()
                    }
                    // Cambiar la visualización a un estado de silencio
                    panelPerformance.setBackgroundColor(Color.DKGRAY) // Color oscuro para indicar pausa
                    txtPerformance.text = "SILENCIO (1 min)"
                    // Opcionalmente, puedes ocultar panelPerformance.visibility = View.GONE
                }
            }

            override fun onResumeCycle() {
                runOnUiThread {
                    Log.e("MainActivity", "Exiting PAUSE CYCLE. Resuming activity.")
                    // Restaurar la visualización
                    panelPerformance.setBackgroundColor(Color.BLACK) // Restaurar color original
                    // El motor rítmico ya habrá reseteado su fase a SOFT_START.
                    // Si este es el servidor, debe reiniciar su turno para comenzar el nuevo ciclo.
                    if (amIServer) {
                        rhythm.performTurn() // El servidor reinicia el ritmo después del silencio
                    } else {
                        // El cliente simplemente espera el mensaje 'TURN' del servidor para reanudar.
                        txtPerformance.text = "WAITING…" // Mostrar esperando mientras el servidor inicia
                    }
                }
            }
        })

        // Set up listener for the "CONNECT" button
        btnConnectIp.setOnClickListener {
            val peerIp = editIp.text.toString().trim()
            if (peerIp.isNotEmpty()) {
                amIServer = false
                Log.d("MainActivity", "CONNECT button clicked. Attempting to connect as client to $peerIp.")
                networkManager.initiateConnection(peerIp)
                txtStatus.text = "Connecting to $peerIp…"
            } else {
                Toast.makeText(this, "Enter peer IP", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up listener for the "START PERFORMANCE" button
        btnStart.setOnClickListener {
            amIServer = true
            rhythm.reset() // Esto establecerá la fase a SOFT_START y reiniciará turnCounter
            txtPerformance.text = ""
            showPerformance()

            if (networkManager.socket == null) {
                Log.d("MainActivity", "START PERFORMANCE clicked. No active socket, starting server.")
                networkManager.startServer()
                txtStatus.text = "Starting server and waiting for peer..."
            } else {
                Log.d("MainActivity", "START PERFORMANCE clicked. Socket active, performing first turn as server.")
                rhythm.performTurn() // Esto iniciará el temporizador del ciclo si es el primer turno
            }
        }
    }

    // Configura todos los MediaPlayers: el principal y los excepcionales.
    private fun setupMediaPlayers() {
        // --- Configuración del MediaPlayer principal ---
        try {
            mainMediaPlayer = MediaPlayer.create(this, R.raw.txalaparta)
            mainMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            mainMediaPlayer?.setOnPreparedListener {
                isMainMediaPlayerPrepared = true
                Log.d("MainActivity", "Main MediaPlayer prepared.")
            }
            mainMediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e("MainActivity", "Main MediaPlayer error: what=$what, extra=$extra")
                isMainMediaPlayerPrepared = false
                Toast.makeText(this, "Main audio playback error!", Toast.LENGTH_SHORT).show()
                true
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up Main MediaPlayer: ${e.message}")
            isMainMediaPlayerPrepared = false
            Toast.makeText(this, "Error loading main audio!", Toast.LENGTH_SHORT).show()
        }

        // --- Configuración de los MediaPlayers excepcionales ---
        // ¡IMPORTANTE!: DEBES CREAR ESTOS ARCHIVOS .WAV (o .MP3) EN TU CARPETA res/raw.
        // Por ejemplo: r1.wav, r2.wav, r3.wav, r4.wav
        val exceptionalResources = arrayOf(
            R.raw.r1, // Asegúrate de que estos recursos existan
            R.raw.r2,
            R.raw.r3,
            R.raw.r4
        )

        totalExceptionalSoundsToLoad = exceptionalResources.size
        exceptionalSoundsPreparedCount = 0
        exceptionalMediaPlayers.clear()

        for (resourceId in exceptionalResources) {
            try {
                val mp = MediaPlayer.create(this, resourceId)
                mp?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mp?.setOnPreparedListener {
                    exceptionalSoundsPreparedCount++
                    if (exceptionalSoundsPreparedCount == totalExceptionalSoundsToLoad) {
                        Log.d("MainActivity", "All exceptional sound MediaPlayers prepared.")
                    }
                }
                mp?.setOnErrorListener { mpError, what, extra ->
                    Log.e("MainActivity", "Exceptional MediaPlayer error for resource $resourceId: what=$what, extra=$extra")
                    Toast.makeText(this, "Exceptional audio error!", Toast.LENGTH_SHORT).show()
                    true
                }
                if (mp != null) {
                    exceptionalMediaPlayers.add(mp)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading exceptional audio resource $resourceId: ${e.message}")
                Toast.makeText(this, "Error loading exceptional audio!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Reproduce un fragmento aleatorio de 400ms del sample de audio principal.
     * @return El offset de inicio del fragmento reproducido.
     */
    private fun playRandomSnippet(): Int {
        try {
            if (mainMediaPlayer == null || !isMainMediaPlayerPrepared) {
                Log.w("MainActivity", "Main MediaPlayer not ready, cannot play random snippet.")
                return 0
            }

            // Asegurarse de no intentar reproducir si estamos en la fase de silencio
            if (rhythm.currentPhase == TxalapartaEngine.PerformancePhase.SILENCE) {
                Log.d("MainActivity", "Attempted to play main snippet during SILENCE phase. Aborting.")
                return 0
            }

            if (mainMediaPlayer!!.isPlaying) {
                mainMediaPlayer!!.pause()
            }

            val duration = mainMediaPlayer!!.duration // Duración total del sample en ms
            val snippetDuration = 400 // Duración del "grano" en ms
            val maxStart = duration - snippetDuration // Offset máximo para que el grano no se corte

            val randomStart = if (maxStart > 0) random.nextInt(maxStart) else 0 // Seleccionar un inicio aleatorio

            mainMediaPlayer!!.seekTo(randomStart) // Ir al punto de inicio aleatorio
            mainMediaPlayer!!.start() // Iniciar reproducción

            // Programar una pausa después de la duración del grano
            handler.postDelayed({
                if (mainMediaPlayer != null && mainMediaPlayer!!.isPlaying) {
                    mainMediaPlayer!!.pause()
                }
            }, snippetDuration.toLong())

            Log.d("MainActivity", "Playing random snippet from offset: $randomStart ms")
            return randomStart
        } catch (e: Exception) {
            Log.e("MainActivity", "Error playing random snippet: ${e.message}")
            return 0
        }
    }

    /**
     * Reproduce un fragmento aleatorio de 400ms de uno de los samples excepcionales.
     */
    private fun playRandomExceptionalSnippet() {
        if (exceptionalMediaPlayers.isEmpty() || exceptionalSoundsPreparedCount != totalExceptionalSoundsToLoad) {
            Log.w("MainActivity", "Exceptional MediaPlayers not fully loaded or list is empty, cannot play exceptional snippet.")
            return
        }

        // Asegurarse de no intentar reproducir si estamos en la fase de silencio
        if (rhythm.currentPhase == TxalapartaEngine.PerformancePhase.SILENCE) {
            Log.d("MainActivity", "Attempted to play exceptional snippet during SILENCE phase. Aborting.")
            return
        }

        try {
            // Seleccionar un MediaPlayer excepcional aleatorio
            val selectedPlayer = exceptionalMediaPlayers[random.nextInt(exceptionalMediaPlayers.size)]

            if (selectedPlayer.isPlaying) {
                selectedPlayer.pause()
            }

            val duration = selectedPlayer.duration // Duración total del sample en ms
            val snippetDuration = 400 // Duración del "grano" en ms
            val maxStart = duration - snippetDuration // Offset máximo para que el grano no se corte

            val randomStart = if (maxStart > 0) random.nextInt(maxStart) else 0 // Seleccionar un inicio aleatorio

            selectedPlayer.seekTo(randomStart) // Ir al punto de inicio aleatorio
            selectedPlayer.start() // Iniciar reproducción

            // Programar una pausa después de la duración del grano
            handler.postDelayed({
                if (selectedPlayer.isPlaying) {
                    selectedPlayer.pause()
                }
            }, snippetDuration.toLong())

            Log.d("MainActivity", "Playing random EXCEPTIONAL snippet from offset: $randomStart ms")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error playing random exceptional snippet: ${e.message}")
        }
    }

    /**
     * Intenta reproducir un fragmento diferente al offset de referencia.
     * (Esta función no se usa en el ciclo rítmico automático actual, pero se mantiene como utilidad).
     * @param referenceOffset El offset de inicio del fragmento anterior.
     */
    private fun playDifferentSnippet(referenceOffset: Int) {
        try {
            if (mainMediaPlayer == null || !isMainMediaPlayerPrepared) {
                Log.w("MainActivity", "Main MediaPlayer not ready, cannot play different snippet.")
                return
            }

            if (mainMediaPlayer!!.isPlaying) {
                mainMediaPlayer!!.pause()
            }

            val duration = mainMediaPlayer!!.duration
            val snippetDuration = 400
            val maxStart = duration - snippetDuration

            val minDistance = 200
            val possibleStarts = (0..maxStart).filter { abs(it - referenceOffset) > minDistance }

            val newOffset = if (possibleStarts.isNotEmpty()) possibleStarts.random() else 0

            mainMediaPlayer!!.seekTo(newOffset)
            mainMediaPlayer!!.start()
            handler.postDelayed({
                if (mainMediaPlayer != null && mainMediaPlayer!!.isPlaying) {
                    mainMediaPlayer!!.pause()
                }
            }, snippetDuration.toLong())

            Log.d("MainActivity", "Playing different snippet from offset: $newOffset ms (ref: $referenceOffset)")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error playing different snippet: ${e.message}")
        }
    }

    // Esta función ahora simplemente llama a playRandomSnippet() del mainMediaPlayer
    private fun playSound() {
        playRandomSnippet()
    }

    // Triggers a brief visual flash on the performance panel.
    private fun triggerHitVisual() {
        runOnUiThread {
            panelPerformance.setBackgroundColor(Color.WHITE)
            Handler(Looper.getMainLooper()).postDelayed({
                panelPerformance.setBackgroundColor(Color.BLACK)
            }, 120)
        }
    }

    // Switches the UI from configuration panel to performance panel.
    private fun showPerformance() {
        runOnUiThread {
            panelConfig.visibility = View.GONE
            panelPerformance.visibility = View.VISIBLE
        }
    }

    // Lifecycle method: release all MediaPlayer resources when activity is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called. Releasing resources.")
        mainMediaPlayer?.release() // Liberar MediaPlayer principal
        mainMediaPlayer = null // Anular referencia

        // Liberar MediaPlayers excepcionales
        for (mp in exceptionalMediaPlayers) {
            mp.release()
        }
        exceptionalMediaPlayers.clear() // Limpiar la lista

        networkManager.closeConnection() // Ensure network connection is closed.
    }
}

