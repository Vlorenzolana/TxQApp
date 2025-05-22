package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val targetIp: String) {
    fun send(message: String) {
        Thread {
            try {

                // Creamos socket para mandar informacion
                val socket = Socket(targetIp, MainActivity.port)

                // Creamos un objeto printWriter que nos permitira escribir (mandar) mensajes
                // Info autoflush usamos autoflush: https://chatgpt.com/share/682ee7dc-6644-8002-8fb1-9f21d67f2be2
                val writer = PrintWriter(socket.getOutputStream(), true)

                // Escribimos el mensaje del parametro en el socket
                writer.println(message)

                // Cerramos el socket
                socket.close()

                Log.i("SOCKET", "Sending meesage: $message")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}