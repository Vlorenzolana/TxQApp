# TxQApp Andriod 4.1 Jelly Bean

Una app experimental para Android que permite la comunicación audiovisual táctil(gotas de lluvia) entre dos tablets conectadas a la misma red local.

## English

The Android application **TxApp (txalakuantika)** establishes a simple peer-to-peer connection using TCP sockets. Each device simultaneously acts as a client and a server, enabling remote tactile interaction(water drops). When the screen is touched on one device, the other device responds by displaying a visual effect (a blinking effect on a random cell within the grid). **TxQApp** has been developed to work with **Sugaarklang**, an interactive sculpture—an audiovisual application inspired by the Basque instrument *txalaparta*—that emulates a natural rainfall cycle while analyzing meteorological prediction models within its unstable ecosystem.

---

## Castellano

La aplicación Android **TxApp (txalakuantika)** establece una conexión sencilla peer-to-peer utilizando sockets TCP. Cada dispositivo actúa simultáneamente como cliente y servidor, permitiendo una interacción táctil(gotas de lluvia) que se refleja remotamente. Al tocar la pantalla en un dispositivo, el otro responde mostrando un efecto visual (parpadeo en una celda aleatoria del grid). **TxQApp** ha sido desarrollada para funcionar junto a **Sugaarklang**, una escultura interactiva—una aplicación audiovisual inspirada en la *txalaparta*—que emula un ciclo natural de lluvia mientras analiza modelos de predicción meteorológica dentro de su ecosistema inestable.

---

## Euskera

Android-erako **TxApp (txalakuantika)** aplikazioak peer-to-peer konexio sinple bat ezartzen du TCP socket-ak erabiliz. Gailu bakoitza bezero eta zerbitzari modura jokatzen du aldi berean, elkarren arteko ukimen(ur ttanttank) bidezko interakzio urrun bat ahalbidetuz. Gailu baten pantaila ukitzean, beste gailuak ikusizko efektu batekin erantzuten du (grid-eko ausazko gelaxka baten keinu distiratsu bat). **TxQApp** aplikazioa **Sugaarklang** eskultura interaktiboarekin batera aritzeko garatua izan da; *txalaparta*n inspiratutako aplikazio ikus-entzunezko bat da, euriaren ziklo natural bat imitatzen duena, eta bere ekosistema ezegonkorrean eguraldiaren iragarpen ereduak aztertzen dituena.


## Version &License

Android 4.1 Jelly Bean, on BQ Marie Curie 2
CC&BY Bcastro & Vlorenzo


## Mapa de directorio

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
│   │   │   └── res/
│   │   │       ├── layout/activity_main.xml
│   │   │       └── raw/beep.wav
```

## Funcionalidades principales

- Detección de toques táctiles.
- Reacción visual: parpadeo de pantalla blanca.
- Reacción sonora: reproducción de un fragmento aleatorio de un sample.
- Comunicación bidireccional vía sockets.
- Interfaz manual para introducir la IP del receptor.

## Requisitos

- Dos dispositivos Android conectados a la misma red WiFi.
- Android Studio con mínimo SDK compatible con MediaPlayer, Handler y Socket.
- Un archivo beep.wav de al menos 2 segundos de duración en: `app/src/main/res/raw/beep.wav`

## Funcionamiento

Cada vez que se toca la pantalla, se reproduce una animación de flash blanco y se selecciona un fragmento aleatorio de un archivo de audio. Este mensaje se transmite por socket al otro dispositivo, que reacciona con un flash y otro fragmento aleatorio del mismo audio.

---



# TxQApp Andriod 4.1 Jelly Bean

An experimental Android app enabling tactile audiovisual communication between two tablets connected to the same local network.

## Directory Map

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
│   │   │   └── res/
│   │   │       ├── layout/activity_main.xml
│   │   │       └── raw/beep.wav
```

## Main Features

- Touch detection and response
- Visual response: full-screen white flash
- Audio response: randomized snippet of a sample
- Bidirectional socket-based communication
- Manual IP entry interface

## Requirements

- Two Android devices on the same WiFi network
- Android Studio with minimum SDK supporting MediaPlayer, Handler, Socket
- A beep.wav file of at least 2 seconds in: `app/src/main/res/raw/beep.wav`

## How it works

Each time the screen is touched, a flash and audio snippet are triggered and sent to the peer device, which responds with its own flash and sound fragment.

---

# TxQApp Andriod 4.1 Jelly Bean

Android-erako aplikazio esperimentala, sare lokal berean konektatutako bi tablet artean ukimen- eta soinu-erreakzioak trukatzeko.

## Direktorioaren egitura

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
│   │   │   └── res/
│   │   │       ├── layout/activity_main.xml
│   │   │       └── raw/beep.wav
```

## Ezaugarri nagusiak

- Ukimenaren detekzioa eta erreakzioa
- Erreakzio bisuala: pantaila zuriz distira
- Erreakzio soinuduna: audio-zatiren ausazko erreprodukzioa
- Bi norabideko komunikazioa socket bidez
- IP helbidea eskuz sartzeko interfazea

## Baldintzak

- WiFi sare berean dauden bi Android gailu
- Android Studio, MediaPlayer, Handler eta Socket onartzen dituen SDKarekin
- Gutxienez 2 segundoko beep.wav fitxategi bat: `app/src/main/res/raw/beep.wav`

## Funtzionamendua

Pantaila ukitzean, pantaila zuriz distira egingo du eta audioaren zatitxo bat joko da. Hori socket bidez bidaltzen zaio beste gailuari, eta honek bere aldetik beste distira eta soinu zatitxo batekin erantzuten du.

---

