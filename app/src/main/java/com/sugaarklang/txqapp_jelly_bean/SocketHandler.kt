package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class SocketHandler(private val port: Int = 12345) {

    private val executor = Executors.newCachedThreadPool()
    private var server: ServerSocket? = null

    fun startServer(onMessage: (event: String, data: String) -> Unit) {
        executor.execute {
            try {
                server = ServerSocket(port)
                while (!server!!.isClosed) {
                    val client = server!!.accept()
                    executor.execute {
                        try {
                            val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                            val message = reader.readLine()
                            if (message != null && message.contains("@")) {
                                val parts = message.split("@", limit = 2)
                                if (parts.size == 2) {
                                    Log.d("SOCKET", "RX: $message")
                                    onMessage(parts[0], parts[1])
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            client.close()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(ip: String, event: String, data: String) {
        executor.execute {
            try {
                val socket = Socket(ip, port)
                val writer = PrintWriter(socket.getOutputStream(), true)
                val msg = "$event@$data"
                writer.println(msg)
                Log.d("SOCKET", "TX: $msg")
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun shutdown() {
        try {
            server?.close()
        } catch (_: Exception) {}
        executor.shutdownNow()
    }
}
