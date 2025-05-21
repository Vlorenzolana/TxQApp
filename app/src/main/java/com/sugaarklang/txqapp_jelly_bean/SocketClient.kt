package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val targetIp: String) {
    fun send(message: String) {
        Thread {
            try {
                Log.i("SOCKET", "Sending meesage: $message")
                val socket = Socket(targetIp, 12345)
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(message)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}