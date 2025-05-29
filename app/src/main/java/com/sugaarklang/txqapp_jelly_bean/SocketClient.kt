package com.sugaarklang.txqapp_jelly_bean

import android.util.Log
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val targetIp: String) {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var connected = false

    fun connect(callback: (() -> Unit)? = null) {
        Thread {
            try {
                socket = Socket(targetIp, PORT)
                writer = PrintWriter(socket!!.getOutputStream(), true)
                connected = true
                Log.i("SOCKET", "Connected to $targetIp")
                callback?.invoke()
            } catch (e: Exception) {
                Log.e("SOCKET", "Connection failed", e)
            }
        }.start()
    }

    fun send(message: String) {
        if (!connected) {
            Log.e("SOCKET", "Not connected, can't send")
            return
        }

        Thread {
            try {
                writer?.println(message)
                Log.i("SOCKET", "Sent: $message")
            } catch (e: Exception) {
                Log.e("SOCKET", "Send error", e)
            }
        }.start()
    }

    fun disconnect() {
        try {
            writer?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e("SOCKET", "Close error", e)
        }
    }
}