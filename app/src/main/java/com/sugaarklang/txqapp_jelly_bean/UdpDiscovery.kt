package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.net.*

class IpDiscovery(
    private val onIpDiscovered: (String) -> Unit,
    private val broadcastPort: Int = 45678 // cualquier puerto UDP libre
) {
    private var running = false

    // Envia un mensaje UDP broadcast periódicamente
    fun startBroadcasting(name: String = "TXQAPP") {
        running = true
        Thread {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true
                val message = "$name@${getLocalIpAddress()}".toByteArray()
                val packet = DatagramPacket(
                    message,
                    message.size,
                    InetAddress.getByName("255.255.255.255"),
                    broadcastPort
                )
                while (running) {
                    socket.send(packet)
                    Thread.sleep(1000)
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("DISCOVERY", "Error broadcasting: ${e.message}")
            }
        }.start()
    }

    // Escucha mensajes UDP broadcast
    fun startListening() {
        running = true
        Thread {
            try {
                val socket = DatagramSocket(broadcastPort, InetAddress.getByName("0.0.0.0"))
                socket.broadcast = true
                while (running) {
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    if (message.startsWith("TXQAPP@")) {
                        val senderIp = packet.address.hostAddress
                        onIpDiscovered(senderIp)
                    }
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("DISCOVERY", "Error listening: ${e.message}")
            }
        }.start()
    }

    fun stop() {
        running = false
    }

    // Utilidad para IP local
    private fun getLocalIpAddress(): String {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
        } catch (ex: Exception) {}
        return ""
    }
}
