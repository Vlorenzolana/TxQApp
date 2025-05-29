package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val targetIp: String) {

    private var socket: Socket? = null

    fun connect() {
        Thread {
            try {
                socket = Socket(targetIp, PORT)
            } catch (e: Exception) {
                Log.e("SOCKET", "Connection failed", e)
            }
        }.start()
    }

    fun send(message: String) {
        Thread {
            try {
                socket?.let {
                    val writer = PrintWriter(it.getOutputStream(), true)
                    writer.println(message)
                    Log.i("SOCKET", "Sent message: $message")
                } ?: Log.e("SOCKET", "Socket is null, unable to send message")
            } catch (e: Exception) {
                Log.e("SOCKET", "Error sending message", e)
            }
        }.start()
    }

    fun disconnect() {
        socket?.close()
    }
}