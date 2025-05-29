package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class BroadcastSender(private val ip: String, private val port: Int = 50000) : Thread() {
    private var running = true

    override fun run() {
        try {
            val socket = DatagramSocket()
            val address = InetAddress.getByName("255.255.255.255")
            val buffer = ip.toByteArray()

            while (running) {
                val packet = DatagramPacket(buffer, buffer.size, address, port)
                socket.send(packet)
                Log.i("BROADCAST", "Sent broadcast with IP: $ip")
                sleep(1000)
            }

            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopBroadcast() {
        running = false
    }
}