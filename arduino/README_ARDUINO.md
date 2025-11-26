# Sleep Tracker IoT - Código Arduino

## 📋 Descripción

Este código permite que un microcontrolador ESP32 recopile datos de múltiples sensores y los envíe a Firebase Realtime Database en tiempo real.

## 🔌 Componentes Necesarios

### Microcontrolador
- **ESP32** (recomendado) o ESP8266 con WiFi

### Sensores (Lectores)
1. **DHT11** - Temperatura y Humedad
2. **MAX30102** - Sensor de pulso cardíaco (SpO2)
3. **ADXL345** - Acelerómetro (detecta movimiento)
4. **LDR** - Fotoresistor (sensor de luz ambiental)

### Actuadores
1. **LED** - Indicador visual de alarma
2. **Buzzer** - Alarma sonora

### Otros
- Resistencias (220Ω para LED, 10kΩ para LDR)
- Protoboard
- Cables jumper

## 📚 Librerías Requeridas

Instala estas librerías desde el Arduino IDE (Sketch > Include Library > Manage Libraries):

```
- WiFi (incluida con ESP32)
- FirebaseESP32 by Mobizt
- DHT sensor library by Adafruit
- Adafruit Unified Sensor
- Adafruit ADXL345
- SparkFun MAX3010x Pulse and Proximity Sensor Library
```

## 🔧 Conexiones del Circuito

### DHT11 (Temperatura y Humedad)
```
DHT11 VCC  -> ESP32 3.3V
DHT11 GND  -> ESP32 GND
DHT11 DATA -> ESP32 GPIO 4
```

### MAX30102 (Sensor de Pulso)
```
MAX30102 VIN -> ESP32 3.3V
MAX30102 GND -> ESP32 GND
MAX30102 SDA -> ESP32 GPIO 21
MAX30102 SCL -> ESP32 GPIO 22
```

### ADXL345 (Acelerómetro)
```
ADXL345 VCC -> ESP32 3.3V
ADXL345 GND -> ESP32 GND
ADXL345 SDA -> ESP32 GPIO 21
ADXL345 SCL -> ESP32 GPIO 22
```

### LDR (Sensor de Luz)
```
LDR Pin 1 -> ESP32 3.3V
LDR Pin 2 -> ESP32 GPIO 34 (ADC)
           -> Resistencia 10kΩ -> GND
```

### LED (Indicador)
```
LED Ánodo (+) -> Resistencia 220Ω -> ESP32 GPIO 2
LED Cátodo (-) -> ESP32 GND
```

### Buzzer (Alarma)
```
Buzzer (+) -> ESP32 GPIO 5
Buzzer (-) -> ESP32 GND
```

## 📐 Diagrama en Tinkercad

### Pasos para crear el circuito en Tinkercad:

1. **Accede a Tinkercad**: https://www.tinkercad.com
2. **Crea un nuevo circuito**
3. **Agrega los componentes**:
   - 1x Arduino Uno (o ESP32 si está disponible)
   - 1x DHT11 (o DHT22)
   - 1x Sensor de temperatura TMP36 (como alternativa si no hay DHT)
   - 1x Fotoresistor (LDR)
   - 1x LED rojo
   - 1x Buzzer/Piezo
   - Resistencias (220Ω, 10kΩ)
   - Protoboard

4. **Conecta según el diagrama de arriba**

5. **Guarda el proyecto** y genera un enlace compartible

**NOTA**: Tinkercad tiene limitaciones en componentes. Para sensores no disponibles (MAX30102, ADXL345), puedes:
- Usar componentes genéricos como bloques de construcción
- Añadir notas explicando qué representa cada componente
- Incluir el diagrama conceptual en la documentación

## ⚙️ Configuración

1. **Copia el archivo de configuración**:
   ```bash
   cp config.h.example config.h
   ```

2. **Edita `config.h`** con tus datos:
   - SSID y contraseña de tu WiFi
   - URL de tu proyecto Firebase
   - Token de autenticación de Firebase

3. **Sube el código**:
   - Abre `sleep_tracker_iot.ino` en Arduino IDE
   - Selecciona la placa correcta (ESP32 Dev Module)
   - Selecciona el puerto COM correcto
   - Click en "Upload"

## 🔥 Configurar Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita **Realtime Database**
4. Configura las reglas de seguridad (para desarrollo):
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```
   ⚠️ **IMPORTANTE**: Estas reglas son solo para desarrollo. En producción, usa reglas más restrictivas.

5. Obtén tu **Database URL** (ej: `tu-proyecto.firebaseio.com`)
6. Obtén tu **Database Secret** en: Project Settings > Service Accounts > Database Secrets

## 📊 Estructura de Datos en Firebase

El código envía datos a Firebase en esta estructura:

```
sleep_tracker/
├── ultimos/                    # Últimos valores de cada sensor
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
└── control/
    └── alarma: false           # Controlado desde la app
```

## 🎮 Control desde la App

El Arduino escucha cambios en:
- `sleep_tracker/control/alarma`: Cuando es `true`, activa el LED y buzzer

## 🐛 Solución de Problemas

### No se conecta a WiFi
- Verifica que el SSID y password sean correctos
- Asegúrate de estar en rango del router
- Verifica que tu red sea 2.4GHz (ESP32 no soporta 5GHz)

### No se conecta a Firebase
- Verifica que la URL de Firebase sea correcta (sin `https://`)
- Verifica que el token de autenticación sea válido
- Revisa las reglas de seguridad en Firebase Console

### Sensores no leen valores
- Verifica las conexiones físicas
- Revisa que las librerías estén instaladas correctamente
- Usa el Serial Monitor (115200 baud) para ver mensajes de error

### MAX30102 no detecta pulso
- Asegúrate de que el dedo esté bien colocado en el sensor
- El sensor necesita unos segundos para estabilizarse
- Verifica que el sensor esté bien conectado (SDA, SCL, VCC, GND)

## 📝 Notas

- El código está optimizado para ESP32, pero puede adaptarse para ESP8266
- Los valores de los sensores se leen cada 2 segundos por defecto
- Se puede cambiar el intervalo modificando `SENSOR_INTERVAL`
- El sistema funciona en modo offline y sincroniza cuando hay conexión

## 🔗 Enlaces Útiles

- [Documentación ESP32](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/)
- [Firebase Arduino Library](https://github.com/mobizt/Firebase-ESP32)
- [DHT Sensor Library](https://github.com/adafruit/DHT-sensor-library)
- [ADXL345 Library](https://github.com/adafruit/Adafruit_ADXL345)
- [MAX30102 Library](https://github.com/sparkfun/SparkFun_MAX3010x_Sensor_Library)
