package com.sugaarklang.txqapp_jelly_bean

import android.os.*
import android.util.Log
import kotlin.math.*

class TxalapartaEngine(val listener: Listener) {

    // Enum directamente dentro de la clase, no en el companion
    enum class PerformancePhase {
        SOFT_START,  // Inicio suave
        GROWTH,      // Crecimiento
        CLIMAX,      // Clímax
        CODA,        // Desaceleración
        SILENCE      // Silencio
    }

    companion object {
        val VARIANTS = arrayOf("hiru", "lau", "improvisado") // Rhythm patterns
        val CURVES = arrayOf("lineal", "exponencial", "logarítmica", "escalonada") // Tempo curves

        private const val ACTIVITY_DURATION_MS = 2 * 60 * 1000L // 2 minutos de actividad
        private const val SILENCE_DURATION_MS = 1 * 60 * 1000L  // 1 minuto de silencio
    }

    interface Listener {
        fun onHit()
        fun onTurnSwitch(isMyTurnNow: Boolean)
        fun onEngineStatusUpdate(status: String)
        fun onPauseCycle()
        fun onResumeCycle()
    }

    private var variant = "hiru"
    private var curve = "lineal"
    private var density = 0.3f
    private var tempoStart = 60

    private var turnCounter = 0
    var currentPhase: PerformancePhase = PerformancePhase.SOFT_START

    private var currentIsMyTurn = false

    private val rhythmHandler = Handler(Looper.getMainLooper())
    private val cycleHandler = Handler(Looper.getMainLooper())
    private val cycleRunnable = object : Runnable {
        override fun run() {
            if (currentPhase != PerformancePhase.SILENCE) {
                enterSilence()
            } else {
                exitSilence()
            }
        }
    }

    fun configure(variant: String, curve: String, density: Float, startTempo: Int) {
        this.variant = variant
        this.curve = curve
        this.density = density
        this.tempoStart = startTempo
        stopCycleTimer()
        this.currentPhase = PerformancePhase.SOFT_START
        this.turnCounter = 0
        listener.onEngineStatusUpdate("Configuración manual: $variant, $curve, $density, $startTempo BPM")
    }

    fun reset() {
        Log.d("TxalapartaEngine", "Resetting engine.")
        stopCycleTimer()
        turnCounter = 0
        currentIsMyTurn = false
        currentPhase = PerformancePhase.SOFT_START
        this.variant = "hiru"
        this.curve = "lineal"
        this.density = 0.3f
        this.tempoStart = 60
        listener.onEngineStatusUpdate("Motor reiniciado. Fase: INICIO SUAVE")
    }

    fun performTurn() {
        if (currentPhase == PerformancePhase.SILENCE) {
            Log.d("TxalapartaEngine", "Cannot perform turn during SILENCE phase. Aborting.")
            listener.onEngineStatusUpdate("Fase: SILENCIO (1 min)")
            return
        }

        currentIsMyTurn = true
        listener.onTurnSwitch(true)

        if (turnCounter == 0 && currentPhase == PerformancePhase.SOFT_START) {
            startCycleTimer(ACTIVITY_DURATION_MS)
            Log.d("TxalapartaEngine", "First turn of new cycle. Activity timer started.")
        }
        turnCounter++

        updateDynamicsForTurn()

        val hits = computeHitsForVariant(variant)
        val intervals = generateIntervals(hits)
        playHits(hits, intervals)
    }

    private fun updateDynamicsForTurn() {
        if (currentPhase == PerformancePhase.SILENCE) {
            listener.onEngineStatusUpdate("Fase: SILENCIO (1 min)")
            return
        }

        val softStartTurns = 5
        val growthTurns = 15
        val climaxTurns = 10
        val totalDynamicTurns = softStartTurns + growthTurns + climaxTurns
        val codaDuration = 10

        when {
            turnCounter <= softStartTurns -> {
                currentPhase = PerformancePhase.SOFT_START
                variant = "hiru"
                curve = "lineal"
                density = 0.3f + (turnCounter.toFloat() / softStartTurns.toFloat()) * 0.3f
                tempoStart = 60
            }
            turnCounter <= softStartTurns + growthTurns -> {
                currentPhase = PerformancePhase.GROWTH
                variant = when (turnCounter % 3) {
                    0 -> "hiru"
                    1 -> "lau"
                    else -> "improvisado"
                }
                curve = "exponencial"
                val progressInPhase = turnCounter - softStartTurns
                density = 0.6f + (progressInPhase.toFloat() / growthTurns.toFloat()) * 0.3f
                tempoStart = 80
            }
            turnCounter <= totalDynamicTurns -> {
                currentPhase = PerformancePhase.CLIMAX
                variant = "improvisado"
                curve = "exponencial"
                density = 0.95f
                tempoStart = 120
            }
            else -> {
                currentPhase = PerformancePhase.CODA
                variant = "hiru"
                curve = "logarítmica"
                val progressInCoda = turnCounter - totalDynamicTurns
                density = 0.95f - (progressInCoda.toFloat() / codaDuration.toFloat()).coerceAtMost(0.45f)
                tempoStart = 180
            }
        }

        density = density.coerceIn(0.0f, 1.0f)
        listener.onEngineStatusUpdate(
            "Turno: $turnCounter, Fase: ${currentPhase.name.replace("_", " ")}, " +
                    "Variante: $variant, Densidad: %.2f, Tempo: %d BPM".format(density, currentTempo())
        )
    }

    private fun computeHitsForVariant(variant: String): Int {
        return when (variant) {
            "hiru" -> 3
            "lau" -> 4
            "improvisado" -> (3..7).random()
            else -> 2
        }
    }

    private fun currentTempo(): Int {
        val now = turnCounter
        val base = tempoStart
        val tempo = when (curve) {
            "lineal" -> base + now * 4
            "exponencial" -> (base * exp(0.045 * now)).toInt()
            "logarítmica" -> (base + 22 * ln((now + 2).toFloat())).toInt()
            "escalonada" -> base + (now / 5) * 18
            else -> base
        }
        return tempo.coerceAtMost(260).coerceAtLeast(60)
    }

    private fun generateIntervals(hits: Int): List<Long> {
        val intervals = mutableListOf<Long>()
        val tempo = currentTempo()
        val beatLen = (60000.0 / tempo).toLong()

        for (i in 0 until hits) {
            if (density < 1.0f && Math.random() > density) {
                intervals.add(beatLen * 2)
            } else {
                intervals.add(beatLen)
            }
        }
        return intervals
    }

    private fun playHits(hits: Int, intervals: List<Long>) {
        var i = 0
        fun next() {
            if (i < hits) {
                if (intervals[i] < (60000.0 / tempoStart) * 1.8) {
                    listener.onHit()
                }
                rhythmHandler.postDelayed({ next() }, intervals[i])
                i++
            } else {
                currentIsMyTurn = false
                listener.onTurnSwitch(false)
            }
        }
        next()
    }

    private fun startCycleTimer(delay: Long) {
        cycleHandler.postDelayed(cycleRunnable, delay)
        Log.d("TxalapartaEngine", "Cycle timer started for ${delay / 1000} seconds.")
    }

    private fun stopCycleTimer() {
        cycleHandler.removeCallbacks(cycleRunnable)
        Log.d("TxalapartaEngine", "Cycle timer stopped.")
    }

    private fun enterSilence() {
        Log.e("TxalapartaEngine", "ENTERING SILENCE PHASE.")
        currentPhase = PerformancePhase.SILENCE
        listener.onPauseCycle()
        listener.onEngineStatusUpdate("Fase: SILENCIO (1 min)")
        stopCycleTimer()
        startCycleTimer(SILENCE_DURATION_MS)
    }

    private fun exitSilence() {
        Log.e("TxalapartaEngine", "EXITING SILENCE PHASE. Restarting cycle.")
        currentPhase = PerformancePhase.SOFT_START
        turnCounter = 0
        listener.onResumeCycle()
        listener.onEngineStatusUpdate("Fase: INICIO SUAVE (Reiniciando ciclo)")
        stopCycleTimer()
    }
}
