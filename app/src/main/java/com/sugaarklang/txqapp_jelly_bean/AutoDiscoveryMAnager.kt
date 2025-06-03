package com.sugaarklang.txqapp_jelly_bean

import android.os.Handler
import android.os.Looper
import java.io.*
import java.net.*
import java.util.concurrent.Executors

/**
 * Descubre automáticamente otro dispositivo en la red y establece una conexión TCP entre ambos.
 * Usa UDP broadcast para anunciarse y escuchar.
 */
class AutoDiscoveryManager(
    val listener: Listener
) {
    interface Listener {
        fun onPeerConnected(asServer: Boolean, tcpSocket: Socket)
        fun onStatus(msg: String)
    }

    private val PORT_TCP = 55888
    private val PORT_UDP = 55899
    private val BROADCAST_MSG = "TXQAPP_DISCOVERY"
    private val executor = Executors.newSingleThreadExecutor()
    private var running = false

    fun start() {
        running = true
        executor.submit {
            try {
                // 1. Empezar a escuchar conexiones TCP entrantes
                val server = ServerSocket(PORT_TCP)
                server.soTimeout = 100 // Short timeout for non-blocking accept

                // 2. Empezar a escuchar mensajes de broadcast
                val udpSocket = DatagramSocket(PORT_UDP, InetAddress.getByName("0.0.0.0"))
                udpSocket.broadcast = true

                val localIp = getLocalIpAddress()
                var peerIp: String? = null
                var tcpSocket: Socket? = null
                listener.onStatus("Buscando compañero…")

                // 3. Lanzar hilo para enviar broadcast periódicamente
                executor.submit {
                    while (tcpSocket == null && running) {
                        try {
                            val data = BROADCAST_MSG.toByteArray()
                            val packet = DatagramPacket(data, data.size, InetAddress.getByName("255.255.255.255"), PORT_UDP)
                            udpSocket.send(packet)
                            Thread.sleep(500)
                        } catch (_: Exception) { }
                    }
                }

                // 4. Escuchar broadcasts entrantes Y tratar de conectar
                val udpBuffer = ByteArray(1024)
                while (tcpSocket == null && running) {
                    // Accept TCP if incoming
                    try {
                        tcpSocket = server.accept()
                        listener.onPeerConnected(true, tcpSocket)
                        running = false
                        udpSocket.close()
                        server.close()
                        return@submit
                    } catch (_: Exception) { /* continue */ }

                    // Escuchar UDP
                    try {
                        val udpPacket = DatagramPacket(udpBuffer, udpBuffer.size)
                        udpSocket.soTimeout = 500
                        udpSocket.receive(udpPacket)
                        val msg = String(udpPacket.data, 0, udpPacket.length)
                        val ip = udpPacket.address.hostAddress
                        if (msg == BROADCAST_MSG && ip != localIp) {
                            peerIp = ip
                            // Intentar conectar como cliente
                            try {
                                val socket = Socket()
                                socket.connect(InetSocketAddress(ip, PORT_TCP), 1200)
                                tcpSocket = socket
                                listener.onPeerConnected(false, tcpSocket)
                                running = false
                                udpSocket.close()
                                server.close()
                                return@submit
                            } catch (_: Exception) { /* No conectado, seguir */ }
                        }
                    } catch (_: Exception) { }
                }
            } catch (e: Exception) {
                listener.onStatus("Error: ${e.message}")
            }
        }
    }

    fun stop() { running = false }

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

