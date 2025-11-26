# Guía Paso a Paso para Crear el Circuito en Tinkercad

## 🎯 Objetivo
Recrear el circuito del Sleep Tracker IoT en Tinkercad para documentación y simulación.

---

## 📝 Paso 1: Crear Cuenta y Proyecto

1. **Acceder a Tinkercad**:
   - Ve a https://www.tinkercad.com
   - Haz clic en "Sign In"
   - Usa tu cuenta de Autodesk (o crea una nueva)

2. **Crear Nuevo Circuito**:
   - En el dashboard, haz clic en "Circuits"
   - Clic en "Create new Circuit"
   - Nombra tu proyecto: "Sleep Tracker IoT"

---

## 🔧 Paso 2: Agregar Componentes

### Componentes Necesarios

Busca y arrastra estos componentes desde el panel lateral:

#### Microcontrolador
- **Arduino Uno R3** (1x)
  - *Nota: Tinkercad no tiene ESP32, usa Arduino Uno como sustituto conceptual*

#### Sensores

1. **DHT22** (1x) - Para temperatura y humedad
   - Busca: "DHT22"
   - Alternativa: Si no está disponible, usa "Temperature Sensor TMP36"

2. **Fotoresistor** (1x) - Para luz ambiental
   - Busca: "Photoresistor" o "LDR"

3. **Acelerómetro** (representación conceptual)
   - *Nota: Tinkercad no tiene ADXL345*
   - Usa un "Module" genérico y etiquétalo como "ADXL345"
   - O agrégalo como texto explicativo

4. **Sensor de Pulso** (representación conceptual)
   - *Nota: Tinkercad no tiene MAX30102*
   - Usa un "Module" genérico y etiquétalo como "MAX30102"
   - O agrégalo como texto explicativo

#### Actuadores

5. **LED Rojo** (1x)
   - Busca: "LED"
   - Selecciona color rojo

6. **Piezo** (1x) - Representa el buzzer
   - Busca: "Piezo"

#### Componentes Pasivos

7. **Resistencias**:
   - 220Ω (1x) - Para LED
   - 10kΩ (1x) - Para fotoresistor

8. **Protoboard** (1x)
   - Tamaño: Full size o Half size

#### Otros

9. **Cables de conexión** - Los necesarios para todas las conexiones

---

## 🔌 Paso 3: Conexiones del Circuito

### 3.1 Conexiones de Alimentación

```
Arduino 5V  → Protoboard línea roja (+)
Arduino GND → Protoboard línea azul/negra (-)
```

### 3.2 Sensor DHT22 (Temperatura y Humedad)

```
DHT22 Pin 1 (+)   → Protoboard 5V (línea roja)
DHT22 Pin 2 (out) → Arduino Pin Digital 4
DHT22 Pin 3       → No conectar
DHT22 Pin 4 (-)   → Protoboard GND (línea azul)
```

**Nota**: Agregar resistencia pull-up de 10kΩ entre Pin 1 y Pin 2 del DHT22 (opcional en Tinkercad)

### 3.3 Fotoresistor (LDR - Sensor de Luz)

```
LDR Pin 1 → Protoboard 5V (línea roja)
LDR Pin 2 → Arduino Pin Analog A0
          → Resistencia 10kΩ → Protoboard GND
```

**Diagrama del divisor de voltaje**:
```
5V ──┬── LDR ──┬── A0
     │         │
     │         └── Resistencia 10kΩ ── GND
```

### 3.4 LED de Alarma

```
LED Ánodo (+) → Resistencia 220Ω → Arduino Pin Digital 2
LED Cátodo (-) → Protoboard GND
```

### 3.5 Buzzer (Piezo)

```
Piezo (+) → Arduino Pin Digital 5
Piezo (-) → Protoboard GND
```

### 3.6 Módulos I2C (Conceptual)

Como Tinkercad no tiene ADXL345 ni MAX30102, puedes:

**Opción 1: Usar bloques genéricos**
- Arrastra 2 "Generic IC" o "Module"
- Etiquétalos:
  - "ADXL345 (Acelerómetro I2C)"
  - "MAX30102 (Sensor Pulso I2C)"
- Dibuja conexiones conceptuales a:
  - VCC → 5V
  - GND → GND
  - SDA → Arduino A4
  - SCL → Arduino A5

**Opción 2: Agregar nota de texto**
- Usa la herramienta de texto
- Escribe: "ADXL345 y MAX30102 conectados vía I2C a A4 (SDA) y A5 (SCL)"

---

## 💻 Paso 4: Agregar Código de Simulación

Haz clic en "Code" y selecciona "Text" (código Arduino).

Copia y pega este código simplificado:

```cpp
/*
 * Sleep Tracker IoT - Simulación Tinkercad
 * Proyecto: Monitoreo de condiciones de sueño
 */

#include <DHT.h>

// Definición de pines
#define DHTPIN 4
#define DHTTYPE DHT22
#define LDR_PIN A0
#define LED_PIN 2
#define BUZZER_PIN 5

// Crear objeto DHT
DHT dht(DHTPIN, DHTTYPE);

// Variables
float temperatura = 0;
float humedad = 0;
int luzValue = 0;
int luzPercent = 0;
bool alarmaActiva = false;

void setup() {
  // Inicializar comunicación serial
  Serial.begin(9600);

  // Configurar pines
  pinMode(LED_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LDR_PIN, INPUT);

  // Inicializar DHT
  dht.begin();

  Serial.println("=== Sleep Tracker IoT ===");
  Serial.println("Sistema iniciado");
  Serial.println();
}

void loop() {
  // Leer sensores
  leerSensores();

  // Mostrar datos
  mostrarDatos();

  // Controlar actuadores (simulación)
  controlarActuadores();

  // Esperar 2 segundos
  delay(2000);
}

void leerSensores() {
  // Leer DHT22
  temperatura = dht.readTemperature();
  humedad = dht.readHumidity();

  // Verificar lecturas
  if (isnan(temperatura) || isnan(humedad)) {
    Serial.println("Error leyendo DHT22");
    temperatura = 0;
    humedad = 0;
  }

  // Leer LDR
  luzValue = analogRead(LDR_PIN);
  luzPercent = map(luzValue, 0, 1023, 0, 100);

  // Simular otros sensores (ya que no están en Tinkercad)
  // En hardware real, aquí se leerían ADXL345 y MAX30102
}

void mostrarDatos() {
  Serial.println("--- Lectura de Sensores ---");

  Serial.print("Temperatura: ");
  Serial.print(temperatura);
  Serial.println(" °C");

  Serial.print("Humedad: ");
  Serial.print(humedad);
  Serial.println(" %");

  Serial.print("Luz: ");
  Serial.print(luzPercent);
  Serial.println(" %");

  Serial.println();
  Serial.println("Nota: ADXL345 y MAX30102 no disponibles en Tinkercad");
  Serial.println("En hardware real:");
  Serial.println("  - Pulso: 72 BPM (MAX30102)");
  Serial.println("  - Aceleración: 0.98 g (ADXL345)");
  Serial.println();

  Serial.println("---------------------------");
  Serial.println();
}

void controlarActuadores() {
  // Simulación simple de alarma
  // Activar si luz es muy alta (>70%) - simulando despertar
  if (luzPercent > 70) {
    alarmaActiva = true;
  } else {
    alarmaActiva = false;
  }

  if (alarmaActiva) {
    digitalWrite(LED_PIN, HIGH);
    tone(BUZZER_PIN, 1000); // Tono de 1kHz
    Serial.println(">>> ALARMA ACTIVA <<<");
  } else {
    digitalWrite(LED_PIN, LOW);
    noTone(BUZZER_PIN);
  }
}
```

---

## ▶️ Paso 5: Simular el Circuito

1. **Iniciar Simulación**:
   - Haz clic en "Start Simulation" (botón verde)

2. **Abrir Serial Monitor**:
   - Haz clic en "Serial Monitor" en la parte inferior
   - Deberías ver los datos de los sensores actualizándose cada 2 segundos

3. **Interactuar con Sensores**:
   - **DHT22**: Ajusta el slider de temperatura y humedad
   - **LDR**: Ajusta el slider de luz
   - **Observa**: Cuando la luz supere 70%, el LED se encenderá y el buzzer sonará

4. **Tomar Capturas**:
   - Captura el circuito completo
   - Captura el Serial Monitor funcionando
   - Captura la lista de componentes

---

## 📸 Paso 6: Guardar y Compartir

1. **Guardar el Proyecto**:
   - Haz clic en el nombre del proyecto arriba
   - Asegúrate de que se guardó correctamente

2. **Crear Enlace Compartible**:
   - Haz clic en "Share" (esquina superior derecha)
   - En "Link sharing", selecciona "Anyone with the link"
   - Copia el enlace
   - Pega el enlace en `PROYECTO.md` sección 3.4

3. **Exportar Capturas**:
   - Usa Print Screen o la herramienta de captura de pantalla
   - Guarda en `/docs/tinkercad/`

---

## 📝 Paso 7: Documentar el Proyecto en Tinkercad

Agrega una descripción al proyecto:

```
Sleep Tracker IoT - Proyecto de Monitoreo de Sueño

Este circuito simula un sistema de monitoreo de condiciones de sueño
que integra:

SENSORES:
- DHT22: Temperatura y humedad ambiente
- LDR: Nivel de luz ambiental
- ADXL345*: Acelerómetro (no disponible en Tinkercad)
- MAX30102*: Sensor de pulso cardíaco (no disponible en Tinkercad)

*Estos sensores se implementan en el prototipo físico real con ESP32

ACTUADORES:
- LED Rojo: Indicador visual de alarma
- Buzzer: Alarma sonora

FUNCIONALIDAD:
El sistema lee sensores cada 2 segundos y activa la alarma
cuando detecta luz intensa (>70%), simulando el despertar.

En el sistema real:
- Datos se envían a Firebase Realtime Database
- App Android permite visualización y control remoto
- Usa ESP32 con WiFi para conectividad IoT

Desarrollado para proyecto de IoT
```

---

## ✅ Checklist de Verificación

Antes de finalizar, verifica que:

- [ ] Todos los componentes están conectados correctamente
- [ ] Las resistencias tienen los valores correctos (220Ω y 10kΩ)
- [ ] El código compila sin errores
- [ ] La simulación funciona
- [ ] El Serial Monitor muestra datos
- [ ] Se tomaron todas las capturas de pantalla
- [ ] Se creó y copió el enlace compartible
- [ ] Se agregó descripción al proyecto

---

## 🎨 Consejos para Mejor Presentación

1. **Organiza los componentes**:
   - Coloca el Arduino a la izquierda
   - Sensores en la parte superior de la protoboard
   - Actuadores en la parte inferior
   - Usa el menor número de cables cruzados

2. **Código de colores para cables**:
   - Rojo: 5V / VCC
   - Negro/Azul: GND
   - Amarillo/Naranja: Señales digitales
   - Verde/Morado: Señales analógicas

3. **Etiquetas**:
   - Usa la herramienta de texto para etiquetar componentes
   - Especialmente para los que no están disponibles en Tinkercad

4. **Notas explicativas**:
   - Agrega cuadros de texto explicando qué mide cada sensor
   - Indica que ESP32 reemplaza a Arduino en el prototipo real

---

## 📚 Recursos Adicionales

- [Tutorial oficial de Tinkercad](https://www.tinkercad.com/learn)
- [Documentación Arduino](https://www.arduino.cc/reference/en/)
- [DHT Sensor Library](https://github.com/adafruit/DHT-sensor-library)

---

## ❓ Solución de Problemas

### La simulación no inicia
- Verifica que no haya errores de compilación
- Revisa las conexiones GND y VCC
- Asegúrate de que todos los pines sean correctos

### El Serial Monitor no muestra datos
- Verifica que el baud rate sea 9600
- Haz clic en "Serial Monitor" después de iniciar la simulación
- Revisa que el código incluya `Serial.begin(9600)`

### Los sensores no leen valores
- Verifica las conexiones de alimentación
- Asegúrate de que los pines coincidan con el código
- Prueba ajustando los sliders de los sensores

---

**¡Listo!** Ahora tienes tu circuito Sleep Tracker IoT simulado en Tinkercad.

Para el prototipo real, reemplaza Arduino Uno con ESP32 y agrega los sensores
I2C reales (ADXL345 y MAX30102).
