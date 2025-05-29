package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket

class BroadcastReceiver(
    private val onIpDiscovered: (String) -> Unit,
    private val port: Int = 50000
) : Thread() {
    private var running = true

    override fun run() {
        try {
            val socket = DatagramSocket(port)
            socket.broadcast = true
            val buffer = ByteArray(256)

            while (running) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val receivedIp = String(packet.data, 0, packet.length)
                Log.i("BROADCAST", "Discovered IP: $receivedIp")
                onIpDiscovered(receivedIp)
            }

            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopReceiver() {
        running = false
    }
}