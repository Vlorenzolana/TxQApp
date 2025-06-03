package com.sugaarklang.txqapp_jelly_bean

import android.os.*
import kotlin.math.*

class TxalapartaEngine(val listener: Listener) {
    companion object {
        val VARIANTS = arrayOf("hiru", "lau", "improvisado")
        val CURVES = arrayOf("lineal", "exponencial", "logarítmica", "escalonada")
    }

    interface Listener {
        fun onHit()
        fun onTurnSwitch(isMyTurnNow: Boolean)
    }

    private var variant = "hiru"
    private var curve = "lineal"
    private var density = 0.3f
    private var tempoStart = 60 // BPM
    private var turnCounter = 0

    private var currentIsMyTurn = false

    private val rhythmHandler = Handler(Looper.getMainLooper())

    fun configure(variant: String, curve: String, density: Float, startTempo: Int) {
        this.variant = variant
        this.curve = curve
        this.density = density
        this.tempoStart = startTempo
    }

    fun reset() {
        turnCounter = 0
        currentIsMyTurn = false
    }

    fun performTurn() {
        currentIsMyTurn = true
        listener.onTurnSwitch(true)
        val hits = computeHitsForVariant(variant)
        val intervals = generateIntervals(hits)
        playHits(hits, intervals)
    }

    private fun computeHitsForVariant(variant: String): Int {
        return when (variant) {
            "hiru" -> 3
            "lau" -> 4
            "improvisado" -> (2..5).random()
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
        return tempo.coerceAtMost(260)
    }

    private fun generateIntervals(hits: Int): List<Long> {
        val intervals = mutableListOf<Long>()
        val tempo = currentTempo()
        val beatLen = (60000.0 / tempo).toLong()
        turnCounter++
        for (i in 0 until hits) {
            if (density < 1.0f && Math.random() > density) {
                intervals.add(beatLen * 2)
            } else {
                intervals.add(beatLen)
            }
        }
        return intervals
    }

    // --- La función que faltaba o podía estar mal pegada ---
    private fun playHits(hits: Int, intervals: List<Long>) {
        var i = 0
        fun next() {
            if (i < hits) {
                if (intervals[i] < 1.5 * (60000.0 / tempoStart)) {
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
}
