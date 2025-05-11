# Aplicación TxQApp

**TxQApp** es fruto de una colaboración abierta entre el desarrollador de software Beñat Castro, cmvgg e Hybridoa.

Esta aplicación es parte del proyecto de escultura interactiva Sugaarklang https://hybridoas-cookbook-recetario.gitbook.io/sugaarklang-semillas-de-folktronica-cuantica TxQapp establece un sistema de **comunicación inalámbrica, audiovisual y bidireccional** entre dos o más dispositivos, activada por la exposición a **condiciones meteorológicas extremas**.

La app habilita los sensores táctiles de nuestros dispositivos, que se activan con la caída de gotas de lluvia sobre las pantallas generando un glitch. Este glitch excita un sistema complejo de predicción meteorológica mediante **semillas de disrupción audiovisual**.

**TxQApp (Txalaquantika Sugaarklang App)** sincroniza dispositivos audiovisuales con eventos meteorológicos y atmosféricos como **tormentas, relámpagos, bruma o xirimiri**.

---

# Flutter + Python + TouchDesigner: Aplicación Audiovisual Interactiva

Esta aplicación es una interfaz móvil desarrollada en **Flutter** que se comunica en tiempo real con un backend audiovisual programado en **Python (OpenCV)** o **TouchDesigner**, permitiendo interacciones visuales y sonoras desde dispositivos Android.

## Características Principales

- **Interfaz táctil Flutter**: Muestra una rejilla interactiva que responde al tacto con sonido o comandos visuales.
- **Backend visual/audio**: Puede usarse con:
  - **Python + OpenCV** para visuales sencillos (streaming de video, detección de movimiento, efectos).
  - **TouchDesigner** para visuales complejos, audio generativo y espectáculos en vivo.
- **Reproducción de sonido local** al interactuar con la interfaz.
- **Simulación de gota de agua** con efecto visual en pantalla.

## Comunicación Bidireccional en Tiempo Real

### Soporte para OSC (Open Sound Control)

- Flutter puede enviar mensajes OSC a TouchDesigner o Python.
- TouchDesigner puede recibir e interpretar esos datos para disparar efectos visuales o sonoros.

```dart
OSC.send(
  '192.168.1.100', // IP de TouchDesigner
  8000,            // Puerto de entrada OSC
  '/trigger',      // Dirección OSC
  [1]              // Payload
);
```

### Soporte para WebSocket

- Se puede usar un servidor WebSocket en Python para permitir una **comunicación bidireccional persistente**.


## Estructura del Proyecto

```
├── flutter_app/
│   ├── lib/
│   │   └── main.dart
│   └── pubspec.yaml
│
├── backend/
│   ├── server.py
│   └── websocket_server.py
│
└── touchdesigner/
    └── visuales.toe
```

## Instalación

### Flutter

```bash
cd flutter_app
flutter pub get
flutter run
```

### Python (OpenCV + Flask)

```bash
cd backend
pip install flask opencv-python numpy
python server.py
```

### TouchDesigner

- Abre `visuales.toe` en TouchDesigner
- Configura el nodo OSC In

## Posibles Usos

- Instalaciones artísticas interactivas
- Escenografía audiovisual en tiempo real
- Interfaces táctiles sonoras
- Experimentos de interacción humano-máquina


## 📄 Licencia

MIT. Uso libre para proyectos educativos o experimentales.