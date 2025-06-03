package com.sugaarklang.txqapp_jelly_bean

import android.content.Context
import java.io.*
import java.net.*
import java.util.concurrent.Executors

class NetworkManager(
    val context: Context,
    val listener: Listener
) {
    interface Listener {
        fun onTurn()
        fun onStatusUpdate(status: String)
    }

    private val PORT = 55888
    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private var inStream: BufferedReader? = null

    private val executor = Executors.newSingleThreadExecutor()

    fun connectToPeer(peerIp: String) {
        executor.submit {
            try {
                // Intenta ser cliente
                val s = Socket()
                s.connect(InetSocketAddress(peerIp, PORT), 3000)
                socket = s
                out = PrintWriter(s.getOutputStream(), true)
                inStream = BufferedReader(InputStreamReader(s.getInputStream()))
                listener.onStatusUpdate("Connected to $peerIp!")
                listenForTurns()
            } catch (e: Exception) {
                listener.onStatusUpdate("Manual connect failed: ${e.message}")
                // Si no conecta, prueba como servidor
                waitAsServer()
            }
        }
    }

    private fun waitAsServer() {
        try {
            listener.onStatusUpdate("Waiting for peer connection…")
            val server = ServerSocket(PORT)
            val s = server.accept()
            socket = s
            out = PrintWriter(s.getOutputStream(), true)
            inStream = BufferedReader(InputStreamReader(s.getInputStream()))
            listener.onStatusUpdate("Peer connected!")
            listenForTurns()
            server.close()
        } catch (e: Exception) {
            listener.onStatusUpdate("Server error: ${e.message}")
        }
    }

    fun sendHit() {
        executor.submit {
            try { out?.println("TURN") } catch (_: Exception) {}
        }
    }

    private fun listenForTurns() {
        while (true) {
            val msg = inStream?.readLine() ?: break
            if (msg.trim() == "TURN") {
                listener.onTurn()
            }
        }
    }

    companion object {
        fun getLocalIpAddress(): String {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val addrs = intf.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            return addr.getHostAddress()
                        }
                    }
                }
            } catch (_: Exception) { }
            return "127.0.0.1"
        }
    }
}
