# 🌙 Sleep Tracker IoT

## Sistema de Monitoreo de Condiciones de Sueño

![Estado](https://img.shields.io/badge/Estado-Activo-success)
![Plataforma](https://img.shields.io/badge/Plataforma-Arduino%20%7C%20Android-blue)
![Firebase](https://img.shields.io/badge/Firebase-Realtime%20Database-orange)

---

## 📋 Descripción del Proyecto

**Sleep Tracker IoT** es una solución completa de Internet de las Cosas (IoT) que monitorea y analiza las condiciones ambientales y biométricas durante el sueño. El sistema integra:

- **Microcontrolador ESP32** con múltiples sensores
- **Firebase Realtime Database** como plataforma en la nube
- **Aplicación móvil Android** para visualización y control en tiempo real

### Problema que Resuelve

Muchas personas experimentan problemas de sueño sin conocer las causas. Este sistema permite:
- Monitorear condiciones ambientales (temperatura, humedad, luz)
- Rastrear indicadores biométricos (pulso cardíaco, movimiento)
- Identificar factores que afectan la calidad del sueño
- Controlar actuadores (alarma) de forma remota

---

## 🎯 Objetivos del Proyecto

1. **Monitoreo en Tiempo Real**: Capturar datos de sensores cada 2 segundos
2. **Almacenamiento en la Nube**: Sincronizar automáticamente con Firebase
3. **Visualización Móvil**: Mostrar datos actualizados en la app Android
4. **Control Remoto**: Activar/desactivar alarma desde el smartphone
5. **Análisis de Patrones**: Identificar condiciones óptimas para dormir

---

## 🔧 Componentes del Sistema

### 1. Hardware (Arduino/ESP32)

#### Microcontrolador
- **ESP32** DevKit v1 (WiFi integrado)

#### Sensores (Lectores)
| Sensor | Función | Rango |
|--------|---------|-------|
| **DHT11** | Temperatura y Humedad | 0-50°C, 20-90% |
| **MAX30102** | Pulso cardíaco (SpO2) | 60-200 BPM |
| **ADXL345** | Acelerómetro | ±2g a ±16g |
| **LDR** | Sensor de luz ambiental | 0-100% |

#### Actuadores
| Actuador | Función |
|----------|---------|
| **LED Rojo** | Indicador visual de alarma |
| **Buzzer Activo** | Alarma sonora |

#### Otros Componentes
- Protoboard 830 puntos
- Resistencias (220Ω, 10kΩ)
- Cables jumper M-M
- Cable USB para alimentación

### 2. Software

#### Código Arduino
- Lenguaje: C++ (Arduino Framework)
- Librerías: WiFi, FirebaseESP32, DHT, ADXL345, MAX30105
- IDE: Arduino IDE 2.x

#### Aplicación Android
- Lenguaje: Kotlin
- Framework: Android SDK 34
- Arquitectura: MVVM con LiveData
- Librerías: Firebase SDK, Material Design 3

#### Plataforma en la Nube
- **Firebase Realtime Database**
- Sincronización bidireccional
- Persistencia local

---

## 📐 Diseño del Circuito

### Diagrama de Conexiones

```
ESP32 DevKit v1
├── GPIO 4  ──────── DHT11 (Data)
├── GPIO 21 ──────── SDA (ADXL345 + MAX30102)
├── GPIO 22 ──────── SCL (ADXL345 + MAX30102)
├── GPIO 34 ──────── LDR (Analog Input)
├── GPIO 2  ──────── LED (+ Resistencia 220Ω)
├── GPIO 5  ──────── Buzzer
├── 3.3V ───────────┬── DHT11 (VCC)
│                   ├── ADXL345 (VCC)
│                   ├── MAX30102 (VCC)
│                   └── LDR (Pin 1)
└── GND ────────────┬── DHT11 (GND)
                    ├── ADXL345 (GND)
                    ├── MAX30102 (GND)
                    ├── LED (Cátodo)
                    ├── Buzzer (-)
                    └── Resistencia 10kΩ (desde LDR Pin 2)
```

### Boceto en Tinkercad

**Enlace al proyecto**: [Pendiente - Crear en Tinkercad]

**Instrucciones para recrear**:
1. Accede a https://www.tinkercad.com
2. Crea un nuevo circuito
3. Agrega los componentes listados arriba
4. Conecta según el diagrama
5. Simula el funcionamiento
6. Guarda y comparte el enlace

**Capturas de pantalla**: Ver carpeta `/docs/tinkercad/`

---

## 🚀 Instalación y Configuración

### Paso 1: Configurar Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto llamado "sleep-tracker-iot"
3. Habilita **Realtime Database**
4. Configura reglas de desarrollo:
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```
5. Copia tu **Database URL** (ej: `sleep-tracker-iot.firebaseio.com`)
6. Ve a **Project Settings > Service Accounts > Database Secrets**
7. Copia tu **Database Secret**

### Paso 2: Configurar el Arduino

1. **Instalar Arduino IDE**:
   - Descarga de https://www.arduino.cc/en/software

2. **Agregar soporte para ESP32**:
   ```
   File > Preferences > Additional Board Manager URLs:
   https://dl.espressif.com/dl/package_esp32_index.json
   ```

3. **Instalar librerías**:
   ```
   Tools > Manage Libraries
   Buscar e instalar:
   - FirebaseESP32 by Mobizt
   - DHT sensor library by Adafruit
   - Adafruit Unified Sensor
   - Adafruit ADXL345
   - SparkFun MAX3010x Pulse and Proximity Sensor Library
   ```

4. **Configurar credenciales**:
   - Abre `arduino/sleep_tracker_iot/config.h`
   - Edita:
     ```cpp
     #define WIFI_SSID "tu_wifi"
     #define WIFI_PASSWORD "tu_password"
     #define FIREBASE_HOST "tu-proyecto.firebaseio.com"
     #define FIREBASE_AUTH "tu-database-secret"
     ```

5. **Cargar código**:
   - Conecta el ESP32 vía USB
   - Selecciona `Tools > Board > ESP32 Dev Module`
   - Selecciona el puerto COM correcto
   - Click en **Upload**

### Paso 3: Configurar la App Android

1. **Descargar google-services.json**:
   - Firebase Console > Project Settings > General
   - Scroll a "Your apps" > Android
   - Click "Add app" si no existe
   - Package name: `com.example.sleeptracker`
   - Descarga `google-services.json`
   - Coloca en `app/google-services.json`

2. **Compilar APK**:
   ```bash
   cd sleeptracker
   ./gradlew assembleDebug
   ```
   El APK estará en: `app/build/outputs/apk/debug/app-debug.apk`

3. **Instalar en dispositivo**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📊 Estructura de Datos en Firebase

```
sleep_tracker/
├── ultimos/                    # Últimos valores (tiempo real)
│   ├── temperatura/
│   │   ├── valor: 22.5
│   │   └── unidad: "°C"
│   ├── humedad/
│   │   ├── valor: 55.0
│   │   └── unidad: "%"
│   ├── pulso/
│   │   ├── valor: 72
│   │   └── unidad: "BPM"
│   ├── aceleracion/
│   │   ├── valor: 0.98
│   │   └── unidad: "g"
│   ├── luz/
│   │   ├── valor: 45
│   │   └── unidad: "%"
│   └── timestamp: 1234567890
│
└── control/                    # Control de actuadores
    └── alarma: false           # true = alarma activa
```

---

## 💻 Uso del Sistema

### Iniciar el Sistema

1. **Conectar el Arduino**:
   - Conecta el ESP32 a una fuente de alimentación USB
   - El sistema se conectará automáticamente a WiFi y Firebase
   - El LED de la placa parpadeará al enviar datos

2. **Abrir la App Android**:
   - Lanza la app "Sleep Tracker"
   - Espera a ver "Conectado a Firebase"
   - Los datos comenzarán a actualizarse automáticamente

### Visualizar Datos

La app muestra en tiempo real:
- **Temperatura**: Color azul (frío), verde (normal), rojo (calor)
- **Humedad**: Color rojo (seco), verde (normal), azul (húmedo)
- **Pulso Cardíaco**: Azul (<60), verde (60-100), amarillo (100-130), rojo (>130 BPM)
- **Aceleración**: Verde (quieto), amarillo (movimiento), rojo (mucho movimiento)
- **Luz Ambiental**: Azul (oscuro), verde (normal), amarillo (muy claro)

### Controlar la Alarma

1. Presiona el botón **"🔔 Activar Alarma"** en la app
2. El botón cambiará a rojo y mostrará **"🔕 Desactivar Alarma"**
3. El Arduino recibirá la señal y activará:
   - LED rojo intermitente
   - Buzzer con tono de 1kHz
4. Presiona nuevamente para desactivar

---

## 🧪 Pruebas de Funcionamiento

### Prueba de Sensores

1. **Temperatura y Humedad**:
   - Coloca un objeto caliente cerca del DHT11
   - Verifica que la temperatura suba en la app
   - Exhala aire húmedo cerca del sensor
   - La humedad debe incrementar

2. **Pulso Cardíaco**:
   - Coloca tu dedo índice sobre el MAX30102
   - Espera 5-10 segundos
   - Debes ver tu pulso en BPM

3. **Aceleración**:
   - Mueve el protoboard suavemente
   - El valor de aceleración debe cambiar
   - Más movimiento = mayor valor

4. **Luz**:
   - Cubre el LDR con tu mano
   - El porcentaje de luz debe bajar
   - Ilumina con una linterna
   - El valor debe subir

### Prueba de Actuadores

1. **Control desde App**:
   - Activa la alarma desde la app
   - Verifica que el LED se encienda intermitente
   - Verifica que el buzzer suene
   - Desactiva y verifica que se apaguen

2. **Sincronización en Tiempo Real**:
   - Abre Firebase Console > Database
   - Cambia manualmente `control/alarma` a `true`
   - Verifica que el Arduino active los actuadores
   - Verifica que la app actualice el botón

---

## 📱 Capturas de Pantalla

### Aplicación Android

![App Principal](docs/screenshots/app_main.png)
*Pantalla principal con todos los sensores*

![Alarma Activa](docs/screenshots/app_alarma.png)
*Control de alarma activado*

### Firebase Console

![Firebase Datos](docs/screenshots/firebase_data.png)
*Datos en tiempo real en Firebase*

### Hardware

![Prototipo](docs/hardware/prototipo_fisico.jpg)
*Prototipo físico en protoboard*

---

## 🛠️ Solución de Problemas

### El Arduino no se conecta a WiFi

**Problema**: "Conectando a WiFi..." sin fin

**Soluciones**:
- Verifica que el SSID y password sean correctos (case-sensitive)
- Asegúrate de que la red sea 2.4GHz (ESP32 no soporta 5GHz)
- Verifica que estés en rango del router
- Reinicia el ESP32

### Datos no aparecen en la App

**Problema**: La app muestra "0.0" en todos los valores

**Soluciones**:
- Verifica que el Arduino esté enviando datos (Serial Monitor)
- Revisa que `google-services.json` esté correctamente configurado
- Verifica que las reglas de Firebase permitan lectura
- Reinicia la app
- Verifica conexión a Internet del smartphone

### Sensores leen valores incorrectos

**Problema**: Lecturas erráticas o constantes en 0

**Soluciones**:
- Revisa las conexiones físicas (VCC, GND, pines de datos)
- Verifica que las librerías estén actualizadas
- Para MAX30102: Asegúrate de que el dedo esté bien colocado
- Para DHT11: Espera 2 segundos entre lecturas
- Para ADXL345: Calibra el sensor si es necesario

### Alarma no se activa

**Problema**: Presionas el botón pero no hay respuesta

**Soluciones**:
- Verifica que el Arduino esté conectado a Firebase
- Revisa Serial Monitor para errores
- Verifica conexión del LED y buzzer
- Prueba cambiar manualmente en Firebase Console

---

## 📈 Conclusiones del Proyecto

### Logros Alcanzados

1. **Integración Exitosa de IoT**:
   - Comunicación bidireccional Arduino ↔ Firebase ↔ App Android
   - Latencia menor a 2 segundos en actualizaciones
   - Sincronización en tiempo real funcionando correctamente

2. **Monitoreo Completo de Sueño**:
   - 5 sensores diferentes capturando datos simultáneos
   - Información biométrica y ambiental integrada
   - Visualización intuitiva con código de colores

3. **Control Remoto Funcional**:
   - Actuadores responden desde la app móvil
   - Feedback visual y sonoro adecuado
   - Sistema de alertas implementado

### Dificultades Encontradas

1. **Configuración de Firebase**:
   - **Problema**: Confusión inicial con las credenciales (API Key vs Database Secret)
   - **Solución**: Documentación clara en README y uso de archivo config.h
   - **Aprendizaje**: Importancia de separar configuración del código

2. **Lectura del MAX30102**:
   - **Problema**: Valores erráticos del sensor de pulso
   - **Solución**: Implementar promedio móvil de 4 lecturas
   - **Aprendizaje**: Los sensores biométricos requieren filtrado de señal

3. **Sincronización de Estados**:
   - **Problema**: La alarma a veces no se sincronizaba entre app y Arduino
   - **Solución**: Implementar listeners bidireccionales en ambos lados
   - **Aprendizaje**: Los sistemas distribuidos requieren manejo cuidadoso de estados

### Conocimientos Adquiridos

1. **Técnicos**:
   - Programación de microcontroladores ESP32
   - Integración de múltiples sensores I2C
   - Desarrollo de apps Android con Kotlin
   - Uso de Firebase Realtime Database
   - Protocolos de comunicación IoT

2. **Desarrollo**:
   - Arquitectura de sistemas IoT
   - Debugging de hardware y software
   - Documentación técnica
   - Gestión de proyectos con Git

---

## 🚀 Propuestas de Mejora

### Mejoras de Funcionalidad

1. **Histórico de Datos**:
   - **Qué**: Guardar lecturas cada 5 minutos en Firebase
   - **Por qué**: Permite analizar tendencias y patrones de sueño a largo plazo
   - **Cómo**: Agregar nodo `sensores/{date}/{timestamp}` en Firebase
   - **Beneficio**: El usuario puede ver gráficas de su sueño durante la semana

2. **Notificaciones Push**:
   - **Qué**: Alertas cuando los valores estén fuera de rango
   - **Por qué**: Prevención proactiva de malas condiciones de sueño
   - **Cómo**: Usar Firebase Cloud Messaging (FCM)
   - **Beneficio**: El usuario recibe avisos si la temperatura es muy alta/baja

3. **Modo Automático de Alarma**:
   - **Qué**: Activar alarma cuando se detecte luz intensa o mucho movimiento
   - **Por qué**: Despertar natural en fase de sueño ligero
   - **Cómo**: Lógica en Arduino que evalúe condiciones
   - **Beneficio**: Mejor calidad de despertar respetando ciclos de sueño

### Mejoras de Diseño

4. **Mejorar UI/UX de la App**:
   - **Qué**: Agregar gráficas en tiempo real, animaciones, tema oscuro
   - **Por qué**: Mejor experiencia de usuario
   - **Cómo**: Usar librerías como MPAndroidChart, Motion Layout
   - **Beneficio**: App más atractiva y fácil de usar

5. **Dashboard Web**:
   - **Qué**: Crear página web para ver datos desde PC
   - **Por qué**: Acceso multiplataforma
   - **Cómo**: Usar React + Firebase SDK
   - **Beneficio**: Análisis más detallado en pantalla grande

### Mejoras Técnicas

6. **Optimizar Consumo de Energía**:
   - **Qué**: Implementar Deep Sleep en ESP32 entre lecturas
   - **Por qué**: Funcionamiento con batería por más tiempo
   - **Cómo**: Usar `esp_deep_sleep()` despertando cada 5 minutos
   - **Beneficio**: Sistema portable sin necesidad de cable USB

7. **Autenticación de Usuarios**:
   - **Qué**: Login con correo/contraseña en la app
   - **Por qué**: Múltiples usuarios pueden usar el sistema
   - **Cómo**: Firebase Authentication + reglas de seguridad
   - **Beneficio**: Cada usuario ve solo sus datos, mayor privacidad

8. **Calibración Automática**:
   - **Qué**: Calibrar sensores al inicio
   - **Por qué**: Mayor precisión en mediciones
   - **Cómo**: Promediar primeras 10 lecturas como baseline
   - **Beneficio**: Compensar variaciones de fabricación

### Mejoras de Hardware

9. **Agregar Sensor de Sonido**:
   - **Qué**: Micrófono para detectar ronquidos
   - **Por qué**: Indicador importante de calidad de sueño
   - **Cómo**: Módulo MAX4466 conectado a pin ADC
   - **Beneficio**: Detección de apnea del sueño

10. **Diseñar PCB Personalizada**:
    - **Qué**: Placa de circuito impreso en lugar de protoboard
    - **Por qué**: Mayor durabilidad y tamaño reducido
    - **Cómo**: Diseñar en KiCad u EasyEDA
    - **Beneficio**: Producto final más profesional y portátil

---

## 🎓 Aplicación Educativa

Este proyecto es ideal para:
- Cursos de IoT y sistemas embebidos
- Prácticas de integración de sensores
- Aprendizaje de Firebase y cloud computing
- Desarrollo de apps móviles Android
- Proyectos de electrónica y programación

---

## 📚 Referencias

### Documentación Oficial
- [ESP32 Datasheet](https://www.espressif.com/sites/default/files/documentation/esp32_datasheet_en.pdf)
- [Firebase Realtime Database](https://firebase.google.com/docs/database)
- [Android Developers](https://developer.android.com)

### Librerías Utilizadas
- [FirebaseESP32](https://github.com/mobizt/Firebase-ESP32)
- [DHT Sensor Library](https://github.com/adafruit/DHT-sensor-library)
- [Adafruit ADXL345](https://github.com/adafruit/Adafruit_ADXL345)
- [SparkFun MAX3010x](https://github.com/sparkfun/SparkFun_MAX3010x_Sensor_Library)

### Tutoriales
- [ESP32 Getting Started](https://randomnerdtutorials.com/getting-started-with-esp32/)
- [Firebase Android Tutorial](https://firebase.google.com/docs/android/setup)

---

## 👥 Autores

**Equipo Sleep Tracker**
- Desarrollo de hardware
- Programación Arduino
- Desarrollo app Android
- Documentación

---

## 📄 Licencia

Este proyecto está bajo licencia MIT. Ver archivo `LICENSE` para más detalles.

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## 📞 Soporte

Para preguntas o soporte:
- Abre un issue en GitHub
- Contacta al equipo: [pendiente agregar email]

---

**¡Gracias por usar Sleep Tracker IoT!** 🌙✨
