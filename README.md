# TxQApp - Dialogo audiovisual entre dos tablets
ENGLISH BELOW
---
## Descripción
TxQApp permite la comunicación reactiva entre dos tablets Android a través de una red WiFi local. Al detectar una interacción táctil ("gota"), se genera una reacción audiovisual (parpadeo de pantalla + sonido). La segunda tablet reacciona con su propia interpretación audiovisual, creando un diálogo interactivo.
---

## Directorio
```
TxQApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sugaarklang/txqapp_jelly_bean/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── TxalapartaEngine.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/activity_main.xml
│   │   │   │   ├── raw/txalaparta.wav
```
---

# TxQApp - Audiovisual Dialogue Between Two Tablets
---
## Description
TxQApp enables reactive communication between two Android tablets over a local Wi-Fi network. Upon detecting a touch interaction ("droplet"), an audiovisual reaction (screen flashing + sound) is generated. The second tablet reacts with its own audiovisual interpretation, creating an interactive dialogue.

---

## Features
- **Local network communication** using `SocketClient` and `SocketServerThread`.
- **Audiovisual dialogue** with:
- A flickering screen.
- A random sound fragment played during each interaction.
- The receiver never repeats the same fragment as the sender thanks to `generateDifferentOffset(...)`.
---

## How to use
1. Connect both tablets to the **same WiFi network**.
2. On the first tablet, press "Start Performance".
3. On the second tablet: enter the IP address of the first tablet. Press "Connect".
4. The rhythm engine begins generating turn-based cycles with dynamic progression.
5. The cycle repeats, alternating minutes of silence.
---

# How the Rhythm Engine Works (TxalapartaEngine)
The app's rhythm engine automatically generates rhythmic beat cycles with dynamic progression and scheduled rests. The engine regulates the **beat pattern**, the **density** (probability of a beat or a rest), and the **tempo**, and after each cycle, forces a period of **total silence** for 1 minute.
---

## Phase and Pattern Cycle
| Phase      | Duration (beats) | Pattern                | Tempo  Curve | Density           | Tempo (BPM)             |
|------------|------------------|------------------------|--------------|-------------------|-------------------------|
| SOFT_START | 5                | "hiru" (3 golpes)      | lineal       | 0.3 → 0.6         | 60 (lento, sube lento)  |
| GROWTH     | 15               | "hiru/lau/improvisado" | exponencial  | 0.6 → 0.9         | 80 (más rápido)         |
| CLIMAX     | 10               | "improvisado" (3-7)    | exponencial  | 0.95 (casi todos) | 120 (muy rápido)        |
| CODA       | 10               | "hiru"                 | logarítmica  | 0.95 → 0.5        | 180→↓ (desacelera)      |
| SILENCE    | 1 min            | (sin golpes ni sonido) | —            | 0.0               | 0                       |
---

### Phase Description

- **SOFT_START:**
  Soft start, simple pattern ("hiru"), few hits, and low tempo. Density gradually increases.

- **GROWTH:**
  The rhythm becomes more complex and varied, alternating patterns. Tempo and density increase more rapidly.

- **CLIMAX:**
  Maximum intensity phase: improvised pattern, almost all possible beats played, high tempo.

- **CODA:**
  Deceleration and simplification: returns to a simple pattern ("hiru"), density and tempo progressively decrease.

- **SILENCE:**
  Total silence (1 minute): no beats, no sound, no visual animation.

After this minute, **the cycle restarts** automatically from SOFT_START.
---

## Visual and Audio

- **Hits and silences**:
  On each turn, the density determines the probability of each beat being present or not (some turns will have pauses).
- **In SILENCE**, there are no hits, sounds, or animations.
---

Developed with ❤️ by Vlorenzolana & BCastro