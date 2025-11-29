# Sleep Tracker IoT - NodeMCU ESP8266

## 📋 Descripción

Este código permite que un **NodeMCU ESP8266** recopile datos de múltiples sensores y los envíe **DIRECTAMENTE** a Firebase Realtime Database en tiempo real, sin necesidad de servidor intermedio.

---

## 🔌 Diferencias: NodeMCU vs ESP32

| Característica | NodeMCU ESP8266 | ESP32 |
|----------------|-----------------|-------|
| **Microcontrolador** | Tensilica L106 (80-160MHz) | Dual-core Xtensa (240MHz) |
| **RAM** | 80 KB | 520 KB |
| **WiFi** | 802.11 b/g/n | 802.11 b/g/n + Bluetooth |
| **Pines GPIO** | 11 utilizables | 34 utilizables |
| **ADC** | 1 pin (10 bits) | 18 pines (12 bits) |
| **Voltaje ADC** | 0-1V (¡CUIDADO!) | 0-3.3V |
| **Librería WiFi** | ESP8266WiFi | WiFi.h |
| **Librería Firebase** | FirebaseESP8266 | FirebaseESP32 |
| **Precio** | ~$3 USD | ~$5 USD |

**Ventaja de NodeMCU**: Más económico, suficiente para este proyecto.

---

## 🛠️ Componentes Necesarios

### Microcontrolador
- **NodeMCU ESP8266** v1.0, v2.0 o v3.0

### Sensores (Lectores)
1. **DHT11** - Temperatura y Humedad
2. **MAX30102** - Sensor de pulso cardíaco (I2C)
3. **ADXL345** - Acelerómetro (I2C)
4. **LDR** - Fotoresistor (sensor de luz ambiental)

### Actuadores
1. **LED Rojo** - Indicador visual de alarma
2. **Buzzer Activo** - Alarma sonora

### Otros
- Resistencias:
  - 220Ω para LED
  - 10kΩ para LDR
  - 100kΩ divisor de voltaje para LDR (IMPORTANTE)
- Protoboard
- Cables jumper
- Cable micro-USB

---

## 📚 Librerías Requeridas

Instala estas librerías desde el Arduino IDE (Sketch > Include Library > Manage Libraries):

```
- ESP8266WiFi (incluida con ESP8266 board manager)
- FirebaseESP8266 by Mobizt
- DHT sensor library by Adafruit
- Adafruit Unified Sensor
- Adafruit ADXL345
- SparkFun MAX3010x Pulse and Proximity Sensor Library
```

### Instalar Soporte para NodeMCU en Arduino IDE

1. **Agregar URL del Board Manager**:
   - File > Preferences > Additional Board Manager URLs
   - Agregar: `http://arduino.esp8266.com/stable/package_esp8266com_index.json`

2. **Instalar el paquete ESP8266**:
   - Tools > Board > Boards Manager
   - Buscar "ESP8266"
   - Instalar "esp8266 by ESP8266 Community"

3. **Seleccionar la placa**:
   - Tools > Board > ESP8266 Boards > NodeMCU 1.0 (ESP-12E Module)

---

## 🔧 Conexiones del Circuito

### ⚠️ IMPORTANTE: NodeMCU usa nomenclatura D0-D8

| Etiqueta Pin | GPIO Real | Función |
|--------------|-----------|---------|
| D0 | GPIO16 | Wake from Deep Sleep |
| D1 | GPIO5 | **SCL (I2C)** |
| D2 | GPIO4 | **SDA (I2C)** |
| D3 | GPIO0 | Flash / LED |
| D4 | GPIO2 | Built-in LED |
| D5 | GPIO14 | SPI CLK |
| D6 | GPIO12 | SPI MISO |
| D7 | GPIO13 | SPI MOSI |
| D8 | GPIO15 | SPI CS |
| A0 | ADC | **Analog Input** |

### DHT11 (Temperatura y Humedad)
```
DHT11 VCC  -> NodeMCU 3.3V
DHT11 GND  -> NodeMCU GND
DHT11 DATA -> NodeMCU D4 (GPIO2)
```

### MAX30102 (Sensor de Pulso) - I2C
```
MAX30102 VIN -> NodeMCU 3.3V
MAX30102 GND -> NodeMCU GND
MAX30102 SDA -> NodeMCU D2 (GPIO4)
MAX30102 SCL -> NodeMCU D1 (GPIO5)
```

### ADXL345 (Acelerómetro) - I2C
```
ADXL345 VCC -> NodeMCU 3.3V
ADXL345 GND -> NodeMCU GND
ADXL345 SDA -> NodeMCU D2 (GPIO4) [compartido con MAX30102]
ADXL345 SCL -> NodeMCU D1 (GPIO5) [compartido con MAX30102]
```

### LDR (Sensor de Luz) - ⚠️ DIVISOR DE VOLTAJE
```
                  ┌─── NodeMCU 3.3V
                  │
                  ├── LDR
                  │
  NodeMCU A0 ─────┤
                  │
                  ├── Resistencia 10kΩ
                  │
                  └─── NodeMCU GND
```

**⚠️ IMPORTANTE**: El pin A0 de NodeMCU solo soporta 0-1V.
Con 3.3V directo se puede dañar. Usa el divisor de voltaje.

### LED (Indicador)
```
LED Ánodo (+) -> Resistencia 220Ω -> NodeMCU D3 (GPIO0)
LED Cátodo (-) -> NodeMCU GND
```

### Buzzer (Alarma)
```
Buzzer (+) -> NodeMCU D8 (GPIO15)
Buzzer (-) -> NodeMCU GND
```

---

## 📐 Diagrama de Conexiones

```
        NodeMCU ESP8266
    ┌─────────────────────┐
    │                     │
    │ 3.3V ──┬────────────┼── DHT11 VCC
    │        ├────────────┼── MAX30102 VIN
    │        ├────────────┼── ADXL345 VCC
    │        └────────────┼── LDR (Pin 1)
    │                     │
    │ GND ───┬────────────┼── DHT11 GND
    │        ├────────────┼── MAX30102 GND
    │        ├────────────┼── ADXL345 GND
    │        ├────────────┼── LED (-)
    │        ├────────────┼── Buzzer (-)
    │        └────────────┼── Resistencia 10kΩ
    │                     │
    │ D1(SCL)─┬───────────┼── MAX30102 SCL
    │         └───────────┼── ADXL345 SCL
    │                     │
    │ D2(SDA)─┬───────────┼── MAX30102 SDA
    │         └───────────┼── ADXL345 SDA
    │                     │
    │ D4 ─────────────────┼── DHT11 DATA
    │                     │
    │ A0 ─────────────────┼── LDR (punto medio)
    │                     │
    │ D3 ─── 220Ω ────────┼── LED (+)
    │                     │
    │ D8 ─────────────────┼── Buzzer (+)
    │                     │
    └─────────────────────┘
```

---

## ⚙️ Configuración

### 1. Configurar Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto: "sleep-tracker-iot"
3. Habilita **Realtime Database**
4. Configura las reglas de seguridad (desarrollo):
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```
   ⚠️ **Para producción, usa reglas más restrictivas.**

5. Copia tu **Database URL** (ej: `sleeptracker-abc123.firebaseio.com`)
   - **SIN** `https://`
   - **SIN** `/` al final

6. Obtén tu **Database Secret**:
   - Project Settings > Service Accounts > Database Secrets
   - Copia el token

### 2. Configurar el Código

1. Abre `config.h`
2. Edita las credenciales:
   ```cpp
   #define WIFI_SSID "TuWiFi"
   #define WIFI_PASSWORD "TuPassword"
   #define FIREBASE_HOST "sleeptracker-abc123.firebaseio.com"
   #define FIREBASE_AUTH "tu-database-secret"
   ```

### 3. Cargar el Código

1. Conecta NodeMCU vía USB
2. Selecciona:
   - Tools > Board > NodeMCU 1.0 (ESP-12E Module)
   - Tools > Port > (tu puerto COM)
   - Tools > Upload Speed > 115200
3. Click en "Upload"
4. Abre Serial Monitor (115200 baud)

---

## 📊 Estructura de Datos en Firebase

El NodeMCU escribe en esta estructura:

```json
{
  "sleep_tracker": {
    "ultimos": {
      "temperatura": {
        "valor": 22.5,
        "unidad": "°C"
      },
      "humedad": {
        "valor": 55.0,
        "unidad": "%"
      },
      "pulso": {
        "valor": 72,
        "unidad": "BPM"
      },
      "aceleracion": {
        "valor": 0.98,
        "unidad": "g"
      },
      "luz": {
        "valor": 45,
        "unidad": "%"
      },
      "timestamp": 1234567890
    },
    "control": {
      "alarma": false
    }
  }
}
```

**Flujo de Datos**:
```
NodeMCU → Firebase → App Android
   ↑                      ↓
   └──────────────────────┘
      (Control de alarma)
```

---

## 🔍 Diagnóstico y Solución de Problemas

### No se conecta a WiFi

**Síntoma**: Serial Monitor muestra "Conectando a WiFi..."

**Soluciones**:
1. Verifica SSID y contraseña (case-sensitive)
2. Asegúrate de que la red sea 2.4GHz (NodeMCU no soporta 5GHz)
3. Verifica que estés en rango del router
4. Prueba con hotspot del celular
5. Revisa Serial Monitor para errores específicos

### No se conecta a Firebase

**Síntoma**: WiFi OK, pero datos no aparecen en Firebase

**Soluciones**:
1. Verifica que `FIREBASE_HOST` NO tenga `https://` ni `/`
2. Verifica que el Database Secret sea correcto
3. Revisa las reglas de Firebase (deben permitir escritura)
4. Verifica en Serial Monitor los mensajes de error
5. Prueba con Postman a la URL de Firebase

### Sensores I2C no detectados

**Síntoma**: "✗ Error: No se detectó ADXL345" o MAX30102

**Soluciones**:
1. Verifica conexiones físicas (SDA=D2, SCL=D1)
2. Usa un escáner I2C para detectar dispositivos:
   ```cpp
   // Código de escaneo I2C
   for(byte address = 1; address < 127; address++) {
     Wire.beginTransmission(address);
     byte error = Wire.endTransmission();
     if (error == 0) {
       Serial.print("Dispositivo en 0x");
       Serial.println(address, HEX);
     }
   }
   ```
3. Verifica que los sensores tengan alimentación (3.3V)
4. Prueba cada sensor individualmente

### LDR lee siempre el mismo valor

**Síntoma**: El valor de luz no cambia

**Soluciones**:
1. Verifica el divisor de voltaje (debe ser correcto)
2. Asegúrate de NO aplicar más de 1V al pin A0
3. Prueba leer el valor crudo: `analogRead(A0)`
4. Cubre el LDR completamente y verifica cambio
5. Mide el voltaje en A0 con multímetro (debe ser <1V)

### Memoria insuficiente

**Síntoma**: "Low memory available", reset constante

**Soluciones**:
1. Reduce el buffer SSL: `firebaseData.setBSSLBufferSize(512, 512);`
2. Aumenta el intervalo de lectura a 5 segundos
3. Desactiva sensores que no uses temporalmente
4. Usa `ESP.getFreeHeap()` para monitorear memoria

---

## 📝 Notas Importantes

### ⚠️ Limitaciones de NodeMCU

1. **Pin A0**: Solo 0-1V (usa divisor de voltaje)
2. **Memoria RAM**: 80KB (optimiza el código)
3. **Solo 1 ADC**: No puedes leer múltiples sensores analógicos simultáneamente
4. **WiFi consume mucha corriente**: Usa fuente de al menos 500mA

### ✅ Optimizaciones

1. **Buffer SSL reducido**:
   ```cpp
   firebaseData.setBSSLBufferSize(1024, 1024);
   ```

2. **Timeout configurado**:
   ```cpp
   config.timeout.serverResponse = 10 * 1000;
   ```

3. **Reconexión WiFi automática**:
   ```cpp
   Firebase.reconnectWiFi(true);
   ```

4. **Monitoreo de memoria**:
   ```cpp
   Serial.print("Memoria libre: ");
   Serial.println(ESP.getFreeHeap());
   ```

---

## 🆚 Comparación de Código: ESP32 vs NodeMCU

| Aspecto | ESP32 | NodeMCU ESP8266 |
|---------|-------|-----------------|
| Librería WiFi | `#include <WiFi.h>` | `#include <ESP8266WiFi.h>` |
| Librería Firebase | `#include <FirebaseESP32.h>` | `#include <FirebaseESP8266.h>` |
| Iniciar WiFi | `WiFi.begin()` | `WiFi.mode(WIFI_STA); WiFi.begin()` |
| Pines I2C | GPIO 21, 22 | D2 (GPIO4), D1 (GPIO5) |
| Iniciar I2C | `Wire.begin()` | `Wire.begin(D2, D1)` |
| ADC | 12 bits (0-4095) | 10 bits (0-1023) |
| Memoria | `ESP.getFreeHeap()` | `ESP.getFreeHeap()` |

---

## 🔗 Enlaces Útiles

- [Documentación ESP8266](https://arduino-esp8266.readthedocs.io/)
- [Firebase ESP8266 Library](https://github.com/mobizt/Firebase-ESP8266)
- [Pinout NodeMCU](https://randomnerdtutorials.com/esp8266-pinout-reference-gpios/)
- [DHT Sensor Library](https://github.com/adafruit/DHT-sensor-library)

---

## 📞 Soporte

Si encuentras problemas:
1. Revisa el Serial Monitor (115200 baud)
2. Verifica las conexiones físicas
3. Prueba con código de ejemplo simple primero
4. Usa el escáner I2C para detectar dispositivos

---

**¡Tu NodeMCU ahora envía datos directamente a Firebase!** 🚀
