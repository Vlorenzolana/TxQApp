package com.sugaarklang.txqapp_jelly_bean

import android.content.Context
import java.io.*
import java.net.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import android.util.Log // Importación añadida para Logcat

class NetworkManager(
    val context: Context,
    val listener: Listener // Listener for network events
) {
    // Interface to communicate network events back to the UI (MainActivity)
    interface Listener {
        fun onTurn() // Called when a "TURN" message is received from the peer
        fun onStatusUpdate(status: String) // Called to update connection status text
        fun onConnected() // Called when a connection is successfully established
    }

    private val PORT = 6000 // The port number for communication
    var socket: Socket? = null // The client socket for communication
    private var out: PrintWriter? = null // Output stream to send data to the peer
    private var inStream: BufferedReader? = null // Input stream to receive data from the peer

    // Separate executors for receiving and sending to prevent deadlocks
    private val receiveExecutor = Executors.newSingleThreadExecutor() // For blocking read operations
    private val sendExecutor = Executors.newSingleThreadExecutor()    // For write operations

    /**
     * Attempts to connect to a peer as a client.
     * This function will block for a maximum of 5 seconds during connection attempt.
     * If connection fails, it reports status but does NOT automatically become a server.
     */
    fun initiateConnection(peerIp: String) {
        // Submit connection logic to the receiveExecutor, as it will also start listening
        receiveExecutor.submit {
            try {
                listener.onStatusUpdate("Attempting to connect to $peerIp...")
                Log.d("NetworkManager", "Attempting client connection to $peerIp:$PORT")
                val s = Socket()
                // Connect with a timeout to prevent indefinite blocking
                s.connect(InetSocketAddress(peerIp, PORT), 5000) // 5 second connection timeout

                // If connection successful, set up streams
                socket = s
                out = PrintWriter(s.getOutputStream(), true) // Auto-flush enabled
                inStream = BufferedReader(InputStreamReader(s.getInputStream()))

                listener.onStatusUpdate("Connected to $peerIp!")
                listener.onConnected() // Notify MainActivity that connection is established
                Log.e("NetworkManager", "CLIENT CONNECTED SUCCESSFULLY to $peerIp.") // CRITICAL LOG
                listenForTurns() // Start listening for incoming messages on this executor
            } catch (e: SocketTimeoutException) {
                listener.onStatusUpdate("Connection to $peerIp timed out. Make sure peer is waiting or IP is correct.")
                Log.e("NetworkManager", "Client connection timed out: ${e.message}")
                closeConnection() // Clean up resources on timeout
            } catch (e: ConnectException) {
                listener.onStatusUpdate("Connection refused by $peerIp. Is the peer app running and waiting?")
                Log.e("NetworkManager", "Client connection refused: ${e.message}")
                closeConnection()
            } catch (e: Exception) {
                listener.onStatusUpdate("Client connection failed: ${e.message}")
                Log.e("NetworkManager", "Client connection failed: ${e.message}")
                closeConnection() // Clean up resources on any other error
            }
        }
    }

    /**
     * Starts a server to wait for an incoming client connection.
     * This function will block for a maximum of 30 seconds waiting for a client.
     */
    fun startServer() {
        // Submit server logic to the receiveExecutor, as it will also start listening
        receiveExecutor.submit {
            var serverSocket: ServerSocket? = null
            try {
                listener.onStatusUpdate("Waiting for peer connection on port $PORT (timeout 30s)...")
                Log.d("NetworkManager", "Starting server and waiting for client on port $PORT.")
                serverSocket = ServerSocket(PORT)
                // Set a timeout for serverSocket.accept() to prevent indefinite blocking
                serverSocket.soTimeout = 30000 // 30 second accept timeout

                val s = serverSocket.accept() // Blocks until a client connects or timeout occurs
                socket = s
                out = PrintWriter(s.getOutputStream(), true) // Auto-flush enabled
                inStream = BufferedReader(InputStreamReader(s.getInputStream()))

                listener.onStatusUpdate("Peer connected!")
                listener.onConnected() // Notify MainActivity that connection is established
                Log.e("NetworkManager", "SERVER ACCEPTED CLIENT CONNECTION.") // CRITICAL LOG
                listenForTurns() // Start listening for incoming messages on this executor
            } catch (e: SocketTimeoutException) {
                listener.onStatusUpdate("Server timed out waiting for connection. No peer connected.")
                Log.e("NetworkManager", "Server accept timed out: ${e.message}")
                closeConnection() // Clean up resources on timeout
            } catch (e: Exception) {
                listener.onStatusUpdate("Server error: ${e.message}")
                Log.e("NetworkManager", "Server error: ${e.message}")
                closeConnection() // Clean up resources on any other error
            } finally {
                // Ensure the ServerSocket is closed after use
                try { serverSocket?.close() } catch (e: Exception) {
                    Log.e("NetworkManager", "Error closing server socket: ${e.message}")
                }
            }
        }
    }

    /**
     * Sends a "TURN" message to the connected peer.
     * This is called by MainActivity when this device's rhythm turn is complete.
     */
    fun sendHit() {
        sendExecutor.submit { // Run send operation on the DEDICATED sendExecutor
            try {
                // Only send if the output stream is available
                if (out != null) {
                    out?.println("TURN")
                    out?.flush() // Explicitly flush the stream to ensure data is sent immediately
                    listener.onStatusUpdate("Sent 'TURN' to peer.")
                    Log.e("NetworkManager", "SUCCESSFULLY SENT 'TURN' MESSAGE.") // CRITICAL LOG
                } else {
                    listener.onStatusUpdate("Cannot send 'TURN': Output stream is null.")
                    Log.e("NetworkManager", "ATTEMPTED TO SEND 'TURN', BUT OUTPUT STREAM IS NULL.") // CRITICAL LOG
                }
            } catch (e: Exception) {
                listener.onStatusUpdate("Failed to send 'TURN': ${e.message}")
                Log.e("NetworkManager", "FAILED TO SEND 'TURN': ${e.message}") // CRITICAL LOG
                closeConnection() // Assume connection is broken if send fails
            }
        }
    }

    /**
     * Continuously listens for incoming messages from the peer.
     * This runs in a loop on the dedicated receiveExecutor until the connection is closed or an error occurs.
     */
    private fun listenForTurns() {
        Log.d("NetworkManager", "Starting to listen for incoming turns. (Loop active)") // Log for loop start
        try {
            // Loop indefinitely to read messages
            while (true) {
                // This log will confirm if the loop is actively waiting for input
                Log.d("NetworkManager", "Waiting to read line from peer...")
                val msg = inStream?.readLine() // This call blocks until a line is read or connection closes
                if (msg == null) {
                    // If readLine returns null, the connection has been closed by the peer
                    listener.onStatusUpdate("Peer disconnected.")
                    Log.e("NetworkManager", "PEER DISCONNECTED (readLine returned null).") // CRITICAL LOG
                    break // Exit the loop
                }
                if (msg.trim() == "TURN") {
                    listener.onStatusUpdate("Received 'TURN' from peer.")
                    Log.e("NetworkManager", "RECEIVED 'TURN' MESSAGE.") // CRITICAL LOG
                    listener.onTurn() // Notify MainActivity to perform its turn
                } else {
                    Log.d("NetworkManager", "Received unknown message: '$msg'")
                }
            }
        } catch (e: IOException) {
            // Handle I/O errors (e.g., connection reset by peer)
            listener.onStatusUpdate("Connection lost: ${e.message}")
            Log.e("NetworkManager", "CONNECTION LOST in listenForTurns: ${e.message}") // CRITICAL LOG
        } catch (e: Exception) {
            // Handle any other unexpected errors
            listener.onStatusUpdate("Error listening for turns: ${e.message}")
            Log.e("NetworkManager", "GENERIC ERROR in listenForTurns: ${e.message}") // CRITICAL LOG
        } finally {
            closeConnection() // Ensure connection is closed after listening loop ends
            Log.e("NetworkManager", "LISTEN FOR TURNS LOOP ENDED, CONNECTION CLOSED.") // CRITICAL LOG
        }
    }

    /**
     * Closes all open network resources (socket, streams).
     * This method is safe to call multiple times.
     */
    fun closeConnection() {
        // Submit closing operations to both executors to ensure all pending tasks are handled
        // and resources are released.
        receiveExecutor.submit {
            Log.d("NetworkManager", "Attempting to close network connection on receiveExecutor.")
            try {
                inStream?.close()
            } catch (e: Exception) {
                Log.e("NetworkManager", "Error closing inStream: ${e.message}")
            }
        }
        sendExecutor.submit {
            Log.d("NetworkManager", "Attempting to close network connection on sendExecutor.")
            try {
                out?.close()
            } catch (e: Exception) {
                Log.e("NetworkManager", "Error closing out: ${e.message}")
            }
        }

        // Close the socket itself, ideally after streams are handled.
        // This can be done on the main thread or a separate small executor if needed for robustness.
        // For simplicity, we'll keep it here, knowing the executors will process their tasks.
        try {
            socket?.close()
            Log.d("NetworkManager", "Socket closed.")
        } catch (e: Exception) {
            Log.e("NetworkManager", "Error closing socket: ${e.message}")
        } finally {
            // Nullify references to allow garbage collection
            inStream = null
            out = null
            socket = null
            Log.d("NetworkManager", "Network resources nulled.")
        }

        // Shut down executors to release their threads
        receiveExecutor.shutdownNow()
        sendExecutor.shutdownNow()
        Log.d("NetworkManager", "Executors shut down.")
    }

    companion object {
        /**
         * Utility function to get the local IPv4 address of the device.
         * Returns "127.0.0.1" if no suitable IP is found.
         */
        fun getLocalIpAddress(): String {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val addrs = intf.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        // Filter for non-loopback IPv4 addresses
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            return addr.getHostAddress()
                        }
                    }
                }
            } catch (e: Exception) {
                // Log the exception for debugging, but return default IP
                Log.e("NetworkManager", "Error getting local IP address: ${e.message}")
            }
            return "127.0.0.1" // Fallback IP
        }
    }
}
