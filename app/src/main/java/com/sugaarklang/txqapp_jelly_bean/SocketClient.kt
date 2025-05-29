package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.PrintWriter
import java.net.Socket

class SocketClient(val targetIp: String) {
    fun send(message: String) {
        Thread {
            try {
                val socket = Socket(targetIp, MainActivity.port)
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(message)
                socket.close()
                Log.i("SOCKET", "Sending message: $message")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}