package com.sugaarklang.txqapp_jelly_bean

import android.os.*
import kotlin.math.*

class TxalapartaEngine(val listener: Listener) {
    companion object {
        val VARIANTS = arrayOf("hiru", "lau", "improvisado")
        val CURVES = arrayOf("lineal", "exponencial", "logarítmica", "escalonada")

        // Definición de las fases de la performance para una progresión rítmica clara.
        enum class PerformancePhase {
            SOFT_START, // Inicio suave: golpes espaciados, tempo lento, baja densidad.
            GROWTH,     // Crecimiento: tempo acelera no lineal, patrones alternan, densidad aumenta.
            CLIMAX,     // Clímax: densidad máxima, patrones complejos, golpes muy próximos.
            CODA        // Coda (opcional): desaceleración, simplificación.
        }
    }

    // Interfaz para comunicar eventos rítmicos y actualizaciones de estado a la UI.
    interface Listener {
        fun onHit() // Llamado cuando ocurre un golpe virtual.
        fun onTurnSwitch(isMyTurnNow: Boolean) // Llamado cuando el turno cambia (hacia o desde este dispositivo).
        fun onEngineStatusUpdate(status: String) // Nuevo método para actualizaciones de estado del motor (fase, tempo, densidad).
    }

    // Parámetros de configuración iniciales para el ritmo.
    private var variant = "hiru"
    private var curve = "lineal"
    private var density = 0.3f // Baja densidad inicial para un inicio suave.
    private var tempoStart = 60 // Tempo inicial lento en BPM (Beats Per Minute).

    private var turnCounter = 0 // Contador de turnos para seguir la progresión.
    private var currentIsMyTurn = false // Indica si es el turno actual de este motor.
    private var currentPhase: PerformancePhase = PerformancePhase.SOFT_START // Fase actual de la performance.

    // Handler para programar tareas con retraso en el hilo principal de la UI (para la sincronización del ritmo).
    private val rhythmHandler = Handler(Looper.getMainLooper())

    /**
     * Configura el motor rítmico con nuevos parámetros.
     * Esto puede usarse para establecer un modo inicial o para anular el comportamiento dinámico.
     * @param variant La cadena del patrón rítmico (ej. "hiru", "lau").
     * @param curve La curva de progresión del tempo.
     * @param density La densidad de golpes (0.0f a 1.0f).
     * @param startTempo El tempo inicial en BPM.
     */
    fun configure(variant: String, curve: String, density: Float, startTempo: Int) {
        this.variant = variant
        this.curve = curve
        this.density = density
        this.tempoStart = startTempo
        // Al configurar manualmente, se reinicia la fase para permitir una nueva progresión dinámica.
        this.currentPhase = PerformancePhase.SOFT_START
        this.turnCounter = 0 // También se reinicia el contador de turnos.
    }

    /**
     * Reinicia el motor rítmico a su estado inicial.
     * Debe llamarse antes de iniciar una nueva performance.
     */
    fun reset() {
        turnCounter = 0
        currentIsMyTurn = false
        currentPhase = PerformancePhase.SOFT_START // Reiniciar la fase al resetear el motor.
        // Reiniciar parámetros dinámicos a valores de inicio suave.
        this.variant = "hiru"
        this.curve = "lineal"
        this.density = 0.3f
        this.tempoStart = 60
    }

    /**
     * Ejecuta un solo turno del ritmo de txalaparta.
     * Esto implica actualizar la dinámica, calcular los golpes y sus intervalos, y reproducir la secuencia.
     */
    fun performTurn() {
        currentIsMyTurn = true
        listener.onTurnSwitch(true) // Notificar que ahora es el turno de este motor.

        turnCounter++ // Incrementar el contador de turnos al inicio de cada turno.

        // Actualizar la dinámica (variante, curva, densidad, tempo base) según el turno y la fase actual.
        updateDynamicsForTurn()

        val hits = computeHitsForVariant(variant) // Determinar el número de golpes para este turno.
        val intervals = generateIntervals(hits) // Calcular los intervalos de tiempo para cada golpe.
        playHits(hits, intervals) // Iniciar la reproducción de la secuencia de golpes.
    }

    /**
     * Actualiza la dinámica del ritmo (variante, curva, densidad) en función del contador de turnos actual
     * y realiza las transiciones entre las fases de la performance.
     * Los puntos de transición y los rangos de valores están definidos para una progresión natural.
     */
    private fun updateDynamicsForTurn() {
        // Puntos de transición de fase (ajustar según la duración deseada de cada fase).
        val softStartTurns = 5    // Duración de la fase de inicio suave.
        val growthTurns = 15      // Duración de la fase de crecimiento (después del inicio suave).
        val climaxTurns = 10      // Duración de la fase de clímax (después del crecimiento).
        val totalDynamicTurns = softStartTurns + growthTurns + climaxTurns // Total de turnos antes de la coda.
        val codaDuration = 10     // Duración de la fase de coda.

        when {
            // FASE 1: INICIO SUAVE
            turnCounter <= softStartTurns -> {
                currentPhase = PerformancePhase.SOFT_START
                variant = "hiru" // Patrón simple al inicio.
                curve = "lineal" // Aumento de tempo suave.
                // Densidad aumenta gradualmente de 0.3 a 0.6 en esta fase.
                density = 0.3f + (turnCounter.toFloat() / softStartTurns.toFloat()) * 0.3f
                tempoStart = 60 // Tempo base lento.
            }
            // FASE 2: CRECIMIENTO
            turnCounter <= softStartTurns + growthTurns -> {
                currentPhase = PerformancePhase.GROWTH
                // Alternar entre "hiru", "lau" e "improvisado" para añadir variedad.
                variant = when (turnCounter % 3) {
                    0 -> "hiru"
                    1 -> "lau"
                    else -> "improvisado"
                }
                curve = "exponencial" // Aceleración de tempo más agresiva.
                // Densidad aumenta gradualmente de 0.6 a 0.9 en esta fase.
                val progressInPhase = turnCounter - softStartTurns
                density = 0.6f + (progressInPhase.toFloat() / growthTurns.toFloat()) * 0.3f
                tempoStart = 80 // Tempo base un poco más alto para la fase de crecimiento.
            }
            // FASE 3: CLÍMAX
            turnCounter <= totalDynamicTurns -> {
                currentPhase = PerformancePhase.CLIMAX
                variant = "improvisado" // Patrones más complejos y aleatorios.
                curve = "exponencial" // Mantener aceleración rápida hasta el final.
                density = 0.95f // Densidad muy alta para el clímax.
                tempoStart = 120 // Tempo base alto para el clímax.
            }
            // FASE 4: CODA (DESACELERACIÓN)
            else -> {
                currentPhase = PerformancePhase.CODA
                variant = "hiru" // Vuelve a patrones más simples.
                curve = "logarítmica" // Curva logarítmica para una desaceleración.
                // Densidad disminuye gradualmente de 0.95 a 0.5.
                val progressInCoda = turnCounter - totalDynamicTurns
                density = 0.95f - (progressInCoda.toFloat() / codaDuration.toFloat()).coerceAtMost(0.45f)
                tempoStart = 180 // Empieza la coda desde un tempo alto para que la logarítmica desacelere.
            }
        }

        // Asegurarse de que la densidad se mantenga dentro de los límites [0.0f, 1.0f].
        density = density.coerceIn(0.0f, 1.0f)

        // Enviar actualización de estado al UI para mostrar la progresión.
        listener.onEngineStatusUpdate(
            "Turno: $turnCounter, Fase: ${currentPhase.name.replace("_", " ")}, " +
                    "Variante: $variant, Densidad: %.2f, Tempo: %d BPM".format(density, currentTempo())
        )
    }

    /**
     * Calcula el número de golpes para una variante de txalaparta dada.
     * @param variant La cadena de la variante rítmica.
     * @return El número de golpes para esta variante.
     */
    private fun computeHitsForVariant(variant: String): Int {
        return when (variant) {
            "hiru" -> 3
            "lau" -> 4
            "improvisado" -> (3..7).random() // Rango más amplio para improvisación en fases avanzadas.
            else -> 2
        }
    }

    /**
     * Calcula el tempo actual basándose en la curva seleccionada y el contador de turnos.
     * El tempo aumenta con el tiempo según la curva.
     * @return El tempo actual en BPM, limitado entre 60 y 260 BPM.
     */
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
        return tempo.coerceAtMost(260).coerceAtLeast(60) // Asegurar que el tempo esté dentro de límites razonables.
    }

    /**
     * Genera una lista de intervalos de tiempo (en milisegundos) para cada golpe en un turno.
     * Incorpora cambios de tempo y densidad para variaciones rítmicas.
     * @param hits El número de golpes en el turno actual.
     * @return Una lista de Long que representa el retraso antes de cada golpe.
     */
    private fun generateIntervals(hits: Int): List<Long> {
        val intervals = mutableListOf<Long>()
        val tempo = currentTempo()
        val beatLen = (60000.0 / tempo).toLong() // Duración de un pulso en milisegundos.

        for (i in 0 until hits) {
            // Aplicar densidad: si un número aleatorio es mayor que la densidad, insertar una pausa más larga (salto de golpe).
            if (density < 1.0f && Math.random() > density) {
                intervals.add(beatLen * 2) // Duplicar la duración del pulso para una pausa.
            } else {
                intervals.add(beatLen) // Duración de pulso regular.
            }
        }
        return intervals
    }

    /**
     * Reproduce la secuencia de golpes con los intervalos generados.
     * Utiliza una función recursiva con postDelayed para programar los golpes.
     * @param hits El número total de golpes a reproducir.
     * @param intervals La lista de retrasos antes de cada golpe.
     */
    private fun playHits(hits: Int, intervals: List<Long>) {
        var i = 0 // Índice para el golpe actual.
        fun next() {
            if (i < hits) {
                // Solo activar un golpe visual/sonoro si el intervalo no es una pausa muy larga
                // (por ejemplo, debido a baja densidad). El umbral (1.8 veces el beatLen inicial) es para evitar que los "silencios" generados por la densidad activen el sonido.
                if (intervals[i] < (60000.0 / tempoStart) * 1.8) {
                    listener.onHit() // Activar un evento de golpe.
                }
                // Programar el siguiente golpe después del intervalo actual.
                rhythmHandler.postDelayed({ next() }, intervals[i])
                i++ // Pasar al siguiente golpe.
            } else {
                // Todos los golpes de este turno han sido reproducidos.
                currentIsMyTurn = false
                listener.onTurnSwitch(false) // Notificar que el turno de este motor ha terminado.
            }
        }
        next() // Iniciar la secuencia.
    }
}
