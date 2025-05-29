package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

class SocketServerThread(
    private val onMessageReceived: (String) -> Unit
) : Thread() {
    override fun run() {
        try {
            val serverSocket = ServerSocket(MainActivity.port)
            while (true) {
                val client = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val message = reader.readLine()
                Log.i("SOCKET", "Message Received: $message")
                if (message != null) {
                    onMessageReceived(message)
                }
                client.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}