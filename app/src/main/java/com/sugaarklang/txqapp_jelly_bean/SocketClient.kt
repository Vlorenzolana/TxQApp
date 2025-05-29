package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val targetIp: String) {
    fun send(message: String) {
        Thread {
            try {
                //Log.i("SOCKET", "Sending message: $message")
                val socket = Socket(targetIp, MainActivity.port)
                // Para que usamos autoflush: https://chatgpt.com/share/682ee7dc-6644-8002-8fb1-9f21d67f2be2
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