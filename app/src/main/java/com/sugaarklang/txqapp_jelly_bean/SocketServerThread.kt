import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

class SocketServerThread(
    private val onMessageReceived: (String) -> Unit
) : Thread() {
    override fun run() {
        try {
            val serverSocket = ServerSocket(12345) // Each tablet listens on the same port
            while (true) {
                val client = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val message = reader.readLine()
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