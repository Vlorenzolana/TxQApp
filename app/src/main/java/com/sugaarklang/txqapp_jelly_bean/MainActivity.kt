package com.sugaarklang.txqapp_jelly_bean

import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

class MainActivity : AppCompatActivity() {

    private lateinit var socketClient: SocketClient
    private lateinit var serverThread: SocketServerThread
    private lateinit var gridView: GridViewCanvas

    // Esta es la forma de crear variables estaticas en kotlin
    companion object {
        val port = 12345 // Puerto en el que van a estar escuchando las tablets y al que van a intentar mandar mensajes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Renderizamos el menu de IP
        setContentView(R.layout.activity_main)

        // Guardamos elementos de la interfaz en variables para poder manipularlas dinamicamente
        val myIpTextView = findViewById<TextView>(R.id.myIpTextView) // Caja donde mostramos la ip propia
        val ipInput = findViewById<EditText>(R.id.ipInput) // Caja donde escribimos la ip de la otra tablet
        val connectButton = findViewById<Button>(R.id.connectButton) // Boton de conectar

        // Mostrar la ip del dispositivo en la interfaz
        val myIp = getLocalIpAddress()
        myIpTextView.text = "Your IP: $myIp"


        // Crear la clase grid view
        // Se le pasa como parametro el callback necesario para mandar la informacion por el socket
        // de que la tablet ha sido pulsada
        gridView = GridViewCanvas(this) { touchedFromRemote ->
            if (!touchedFromRemote) {
                socketClient.send("TOUCH")
            }
        }

        // Inicializacion del servidor (escucha de mensajes)
        serverThread = SocketServerThread { message ->
            if (message == "TOUCH") {
                runOnUiThread {
                    gridView.blinkFromRemote()
                }
            }
        }
        serverThread.start()

        // Le damos funcionalidad al boton
        connectButton.setOnClickListener {

            // Se obtiene la ip desde la interfaz
            val targetIp = ipInput.text.toString().trim()

            // Si la IP no esta vacia, creamos un socket para mandarle mensajes a la otra tablet
            if (targetIp.isNotEmpty()) {
                socketClient = SocketClient(targetIp)
                runOnUiThread {

                    // Cambiamos el menu de input de IP a cuadricula
                    transitionToGrid(ipInput)
                }
            }
        }
    }


    // Funcion que devuelva la ip del propio dispositivo
    // Usada para mostrar la IP propia en el menu inicial
    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }

    private fun transitionToGrid(ipInput: EditText) {

        // Oculta el teclado si está abierto
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ipInput.windowToken, 0)

        // Oculta la barra de acción (ActionBar) si se está usando AppCompatActivity
        supportActionBar?.hide()

        // Activa el modo de pantalla completa inmersivo
        // Estas flags permiten que la app utilice toda la pantalla y oculte la barra de estado y navegación
        // En Android 4.2.2 (API 17), este es el método compatible para lograr un modo fullscreen moderno
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY           // Permite que la UI se oculte automáticamente tras gestos
                        or View.SYSTEM_UI_FLAG_FULLSCREEN       // Oculta la barra de estado (parte superior)
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  // Oculta la barra de navegación (parte inferior)
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN        // Permite que el contenido se dibuje detrás de la barra de estado
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION   // Permite que el contenido se dibuje detrás de la barra de navegación
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE             // Mantiene la disposición estable al cambiar la visibilidad del sistema UI
                )

        // Establece explícitamente el modo de pantalla completa usando WindowManager
        // Esto es redundante con las flags anteriores pero necesario en versiones antiguas para garantizar el efecto
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Finalmente, se establece el layout principal (en este caso, el GridView)
        setContentView(gridView)
    }
}
