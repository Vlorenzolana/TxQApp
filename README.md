# TxQApp

---

## Descripción

Two Android tablets can “talk” to each other by touching the screen. Each touch produces a short sound and a visual flash, and that event is sent to the other tablet via network (TCP socket).
There is a manual connection mode: you enter the IP of the other tablet and tap Connect.

## Features

- Local communication using sockets.
- Sound sample divided into small random fragments.
- Second device always plays a different snippet (not the same offset as the first).

## File structure and Logic

1. MainActivity.kt

   Responsible for the initial interface:

        Displays the local IP.

        Allows you to enter the remote IP.

        Button to connect.

   Logic:

        When you click “Connect”, it creates:

            SocketClient: to send events to the remote IP.

            SocketServerThread: to receive events from the other tablet.

            GridViewCanvas: the canvas where touches and flashes occur.

        Switches the view to the grid and hides the navigation bar.

2. GridViewCanvas.kt

   Custom view:

        Occupies the entire screen and manages touches.

        Each local touch:

            Calls onTouchLocal: sends an event per socket.

            Flashes and plays a “snippet” of the audio.

        Each remote touch received:

            Calls onTouchRemote: plays a different snippet and flashes.

3. SocketClient.kt

   TCP client:

        Connects to the IP of the other tablet, to the fixed port (12345).

        Sends messages of type EVENT@DATA (e.g. TOUCH@1023).

4. SocketServerThread.kt

   TCP server (on each tablet):

        Listens for connections on port 12345.

        When receiving an EVENT@DATA message, it executes the received function (onMessageReceived).

5. activity_main.xml

   Startup interface:

        Displays local IP.

        Field for remote IP.

        Connect button.

6. AndroidManifest.xml

   Required permissions:

        Internet (for sockets).

        Wi-Fi status (to get local IP).

## Interaction flow

    Startup:
    The app displays the local IP and asks for the remote IP.

    Connect:

        Starts the socket server to listen for incoming events.

        The button creates the client and the new interaction view.

    Tap on the screen:

        Socket sends the event and displays the local flash.

        The other tablet receives the event, plays sound and displays remote flash.

---

## Folder Architecture

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

Developed by vlorenzolana & BCastro