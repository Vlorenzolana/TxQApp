package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class SocketServerThread(
    private val onMessageReceived: (String, String) -> Unit
) : Thread() {
    override fun run() {
        try {
            val serverSocket = ServerSocket(MainActivity.port)
            while (true) {
                val client = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val message = reader.readLine()
                val clientIp = client.inetAddress.hostAddress
                Log.i("SOCKET", "Message Received from $clientIp: $message")
                if (message != null) {
                    onMessageReceived(message, clientIp)
                }
                client.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}