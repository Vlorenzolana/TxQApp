# TxQApp - Comunicación audiovisual entre dos tablets

---

## 🧠 Descripción general

TxQApp permite la comunicación reactiva entre dos tablets Android a través de una red WiFi local. Al detectar una interacción táctil ("gota"), se genera una reacción audiovisual (parpadeo de pantalla + sonido). La segunda tablet reacciona con su propia interpretación audiovisual, creando un diálogo interactivo.

---

## 📁 Mapa de directorio

```
TxQApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sugaarklang/txqapp_jelly_bean/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── GridViewCanvas.kt
│   │   │   │   ├── SocketClient.kt
│   │   │   │   ├── SocketServerThread.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/activity_main.xml
│   │   │   │   ├── raw/beep.wav
```

---

## ✨ Funciones clave

- **Comunicación en red local** usando `SocketClient` y `SocketServerThread`.
- **Diálogo audiovisual** con:
    - Pantalla que parpadea.
    - Fragmento de sonido aleatorio reproducido en cada interacción.
    - El receptor nunca repite el mismo fragmento que el emisor gracias a `generateDifferentOffset(...)`.

---

## 🧪 Cómo usar

1. Conecta ambas tablets a la **misma red WiFi**.
2. En la primera tablet, introduce la IP de la segunda tablet.
3. Presiona "Connect".
4. Al tocar la pantalla, se desencadena una acción-reacción.
5. El ciclo se repite alternando roles.

---

## 🧩 Código fuente destacado

**Función auxiliar `generateDifferentOffset()`**
```kotlin
private fun generateDifferentOffset(referenceOffset: Int, duration: Int): Int {
    val maxStart = duration - 100
    val range = (0..maxStart).filter { kotlin.math.abs(it - referenceOffset) > 200 }
    return if (range.isNotEmpty()) range.random() else 0
}
```

---

# TxQApp - Elkarrizketa bisual eta akustikoa bi tablet artean

## 🧠 Deskribapena

TxQApp-ek bi tablet-en artean elkarreragiteko sistema sortzen du. Ukipen batek (esaterako, ur tanta bat) ekintza pizten du eta bigarren tablet-ak erantzun propio bat sortzen du.

## 🔧 Funtzionalitateak

- Tablet bakoitzak entzuten eta bidaltzen du bere IP-rekin.
- Pantaila zuriz piztu eta itzaltzen da.
- Audio-lagin baten zati txiki bat jotzen da ausaz.
- Bigarren tabletak ez du inoiz errepikatzen lehenengoaren zatia.

---

# TxQApp - Audio-visual tablet interaction

## 🧠 Description

TxQApp creates a reactive audiovisual dialogue between two Android tablets over local WiFi. A screen touch on one device triggers a flash and sound snippet. The second device responds with its own audiovisual version.

## 🔧 Features

- Local communication using sockets.
- Sound sample divided into small random fragments.
- Second device always plays a different snippet (not the same offset as the first).

---

Developed with ❤️ by [Vanessa Lorenzo]rsión, al tocar la pantalla se genera un fragmento aleatorio del sonido y se envía el offset exacto al otro dispositivo. El receptor escoge otro fragmento distinto automáticamente, garantizando variedad sonora en cada diálogo.