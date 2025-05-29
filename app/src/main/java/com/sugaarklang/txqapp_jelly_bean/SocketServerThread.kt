package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.SocketException

class SocketServerThread(private val onMessageReceived: (String) -> Unit) : Thread() {
    private lateinit var serverSocket: ServerSocket

    override fun run() {
        try {
            serverSocket = ServerSocket(PORT)
            while (!interrupted()) {
                val client = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val message = reader.readLine()
                Log.i("SOCKET", "Message Received: $message")
                if (message != null) {
                    onMessageReceived(message)
                }
                client.close()
            }
        } catch (e: SocketException) {
            Log.e("SOCKET", "Server socket closed", e)
        } catch (e: Exception) {
            Log.e("SOCKET", "Server error", e)
        }
    }

    fun stopServer() {
        interrupt()
        serverSocket.close()
    }
}