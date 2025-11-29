/*
 * Sleep Tracker IoT - NodeMCU ESP8266 con Firebase
 *
 * Este proyecto monitorea condiciones de sueño usando múltiples sensores:
 * - DHT11: Temperatura y Humedad
 * - MAX30102: Sensor de pulso cardíaco y oxígeno
 * - ADXL345: Acelerómetro (detecta movimiento)
 * - LDR: Sensor de luz ambiental
 * - LED/Buzzer: Actuador para alarma
 *
 * Los datos se envían DIRECTAMENTE a Firebase Realtime Database
 * y pueden ser monitoreados desde una aplicación Android en tiempo real.
 *
 * Hardware: NodeMCU ESP8266 (v1.0 o v2.0)
 * Autor: Equipo Sleep Tracker
 * Fecha: 2024
 */

// ============= LIBRERÍAS =============
#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include <DHT.h>
#include <Wire.h>
#include <Adafruit_ADXL345_U.h>
#include "MAX30105.h"
#include "heartRate.h"

// ============= CONFIGURACIÓN WIFI =============
#define WIFI_SSID "TU_WIFI_SSID"
#define WIFI_PASSWORD "TU_WIFI_PASSWORD"

// ============= CONFIGURACIÓN FIREBASE =============
#define FIREBASE_HOST "tu-proyecto.firebaseio.com"
#define FIREBASE_AUTH "tu-token-de-autenticacion"

// ============= CONFIGURACIÓN DE PINES NodeMCU =============
// Mapeo de pines para NodeMCU:
// D1 = GPIO5, D2 = GPIO4, D3 = GPIO0, D4 = GPIO2
// D5 = GPIO14, D6 = GPIO12, D7 = GPIO13, D8 = GPIO15

#define DHTPIN D4             // GPIO2 - Pin para DHT11
#define DHTTYPE DHT11         // Tipo de sensor DHT
#define LDR_PIN A0            // Pin analógico para el sensor de luz
#define LED_PIN D3            // GPIO0 - Pin para LED de alerta
#define BUZZER_PIN D8         // GPIO15 - Pin para buzzer de alarma

// Pines I2C para NodeMCU (por defecto):
// SDA: D2 (GPIO4)
// SCL: D1 (GPIO5)

// ============= OBJETOS DE SENSORES =============
DHT dht(DHTPIN, DHTTYPE);
Adafruit_ADXL345_Unified accel = Adafruit_ADXL345_Unified(12345);
MAX30105 particleSensor;
FirebaseData firebaseData;
FirebaseConfig config;
FirebaseAuth auth;

// ============= VARIABLES GLOBALES =============
// Intervalos de lectura
unsigned long lastSensorRead = 0;
const unsigned long SENSOR_INTERVAL = 2000; // Leer sensores cada 2 segundos

// Variables de sensores
float temperatura = 0.0;
float humedad = 0.0;
int pulsoCardiaco = 0;
float aceleracion = 0.0;
int nivelLuz = 0;

// Variables para detección de pulso
const byte RATE_SIZE = 4;
byte rates[RATE_SIZE];
byte rateSpot = 0;
long lastBeat = 0;
float beatsPerMinute;
int beatAvg;

// Estado de la alarma (controlado desde Firebase)
bool alarmaActiva = false;

// ============= SETUP =============
void setup() {
  Serial.begin(115200);
  Serial.println("\n\n=== Sleep Tracker IoT - NodeMCU ===");

  // Configurar pines
  pinMode(LED_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LDR_PIN, INPUT);

  // Apagar actuadores al inicio
  digitalWrite(LED_PIN, LOW);
  digitalWrite(BUZZER_PIN, LOW);

  // Inicializar sensores
  inicializarSensores();

  // Conectar a WiFi
  conectarWiFi();

  // Configurar Firebase
  configurarFirebase();

  Serial.println("Sistema iniciado correctamente!");
}

// ============= LOOP PRINCIPAL =============
void loop() {
  unsigned long currentMillis = millis();

  // Leer sensores periódicamente
  if (currentMillis - lastSensorRead >= SENSOR_INTERVAL) {
    lastSensorRead = currentMillis;

    leerSensores();
    enviarDatosFirebase();

    // Mostrar datos en Serial Monitor
    mostrarDatos();
  }

  // Verificar estado de alarma desde Firebase
  verificarAlarma();

  // Controlar actuadores
  controlarActuadores();

  delay(100);
}

// ============= FUNCIONES DE INICIALIZACIÓN =============

void inicializarSensores() {
  Serial.println("Inicializando sensores...");

  // Inicializar I2C con pines específicos de NodeMCU
  Wire.begin(D2, D1); // SDA=D2(GPIO4), SCL=D1(GPIO5)

  // Inicializar DHT11
  dht.begin();
  Serial.println("✓ DHT11 inicializado");

  // Inicializar ADXL345
  if (!accel.begin()) {
    Serial.println("✗ Error: No se detectó ADXL345");
    Serial.println("  Verifica las conexiones I2C (SDA=D2, SCL=D1)");
  } else {
    accel.setRange(ADXL345_RANGE_16_G);
    Serial.println("✓ ADXL345 inicializado");
  }

  // Inicializar MAX30102
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
    Serial.println("✗ Error: No se detectó MAX30102");
    Serial.println("  Verifica las conexiones I2C (SDA=D2, SCL=D1)");
  } else {
    particleSensor.setup();
    particleSensor.setPulseAmplitudeRed(0x0A);
    particleSensor.setPulseAmplitudeGreen(0);
    Serial.println("✓ MAX30102 inicializado");
  }

  Serial.println("Sensores inicializados!");
}

void conectarWiFi() {
  Serial.print("Conectando a WiFi");
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int intentos = 0;
  while (WiFi.status() != WL_CONNECTED && intentos < 30) {
    delay(500);
    Serial.print(".");
    intentos++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✓ WiFi conectado!");
    Serial.print("Dirección IP: ");
    Serial.println(WiFi.localIP());
    Serial.print("Señal WiFi: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
  } else {
    Serial.println("\n✗ Error: No se pudo conectar a WiFi");
    Serial.println("Verifica SSID y contraseña");
  }
}

void configurarFirebase() {
  Serial.println("Configurando Firebase...");

  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;

  // Configurar timeout para NodeMCU
  config.timeout.serverResponse = 10 * 1000; // 10 segundos

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  // Configurar tamaño de buffer para NodeMCU
  firebaseData.setBSSLBufferSize(1024, 1024);

  Serial.println("✓ Firebase configurado!");
}

// ============= FUNCIONES DE LECTURA DE SENSORES =============

void leerSensores() {
  // Leer DHT11 (Temperatura y Humedad)
  float tempReading = dht.readTemperature();
  float humReading = dht.readHumidity();

  // Verificar si las lecturas son válidas
  if (!isnan(tempReading) && !isnan(humReading)) {
    temperatura = tempReading;
    humedad = humReading;
  } else {
    Serial.println("⚠ Error leyendo DHT11");
  }

  // Leer ADXL345 (Aceleración)
  sensors_event_t event;
  accel.getEvent(&event);

  // Calcular magnitud de aceleración
  float x = event.acceleration.x;
  float y = event.acceleration.y;
  float z = event.acceleration.z;
  aceleracion = sqrt(x*x + y*y + z*z) / 9.8; // Normalizar a g

  // Leer MAX30102 (Pulso Cardíaco)
  long irValue = particleSensor.getIR();

  if (checkForBeat(irValue) == true) {
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20) {
      rates[rateSpot++] = (byte)beatsPerMinute;
      rateSpot %= RATE_SIZE;

      // Calcular promedio
      beatAvg = 0;
      for (byte x = 0; x < RATE_SIZE; x++) {
        beatAvg += rates[x];
      }
      beatAvg /= RATE_SIZE;
      pulsoCardiaco = beatAvg;
    }
  }

  // Si no hay dedo detectado, poner valor por defecto
  if (irValue < 50000) {
    pulsoCardiaco = 0;
  }

  // Leer LDR (Sensor de Luz)
  int valorLDR = analogRead(LDR_PIN);
  nivelLuz = map(valorLDR, 0, 1023, 0, 100); // NodeMCU: ADC de 10 bits (0-1023)
}

// ============= FUNCIONES DE FIREBASE =============

void enviarDatosFirebase() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("⚠ WiFi desconectado, reintentando...");
    conectarWiFi();
    return;
  }

  String basePath = "sleep_tracker/ultimos";

  // Enviar temperatura
  if (Firebase.setFloat(firebaseData, basePath + "/temperatura/valor", temperatura)) {
    Firebase.setString(firebaseData, basePath + "/temperatura/unidad", "°C");
  } else {
    Serial.println("✗ Error enviando temperatura: " + firebaseData.errorReason());
  }

  // Enviar humedad
  if (Firebase.setFloat(firebaseData, basePath + "/humedad/valor", humedad)) {
    Firebase.setString(firebaseData, basePath + "/humedad/unidad", "%");
  } else {
    Serial.println("✗ Error enviando humedad: " + firebaseData.errorReason());
  }

  // Enviar pulso
  if (Firebase.setInt(firebaseData, basePath + "/pulso/valor", pulsoCardiaco)) {
    Firebase.setString(firebaseData, basePath + "/pulso/unidad", "BPM");
  } else {
    Serial.println("✗ Error enviando pulso: " + firebaseData.errorReason());
  }

  // Enviar aceleración
  if (Firebase.setFloat(firebaseData, basePath + "/aceleracion/valor", aceleracion)) {
    Firebase.setString(firebaseData, basePath + "/aceleracion/unidad", "g");
  } else {
    Serial.println("✗ Error enviando aceleración: " + firebaseData.errorReason());
  }

  // Enviar luz
  if (Firebase.setInt(firebaseData, basePath + "/luz/valor", nivelLuz)) {
    Firebase.setString(firebaseData, basePath + "/luz/unidad", "%");
  } else {
    Serial.println("✗ Error enviando luz: " + firebaseData.errorReason());
  }

  // Guardar timestamp
  Firebase.setInt(firebaseData, basePath + "/timestamp", millis());
}

void verificarAlarma() {
  // Leer estado de alarma desde Firebase
  if (Firebase.getBool(firebaseData, "sleep_tracker/control/alarma")) {
    alarmaActiva = firebaseData.boolData();
  }
}

// ============= FUNCIONES DE CONTROL =============

void controlarActuadores() {
  if (alarmaActiva) {
    // Activar LED y buzzer con patrón intermitente
    static unsigned long lastBlink = 0;
    static bool estadoLED = false;

    if (millis() - lastBlink > 500) {
      lastBlink = millis();
      estadoLED = !estadoLED;

      digitalWrite(LED_PIN, estadoLED ? HIGH : LOW);

      if (estadoLED) {
        tone(BUZZER_PIN, 1000); // Tono de 1kHz
      } else {
        noTone(BUZZER_PIN);
      }
    }
  } else {
    // Apagar actuadores
    digitalWrite(LED_PIN, LOW);
    noTone(BUZZER_PIN);
  }
}

// ============= FUNCIONES AUXILIARES =============

void mostrarDatos() {
  Serial.println("\n--- Lectura de Sensores ---");
  Serial.print("Temperatura: ");
  Serial.print(temperatura);
  Serial.println(" °C");

  Serial.print("Humedad: ");
  Serial.print(humedad);
  Serial.println(" %");

  Serial.print("Pulso: ");
  Serial.print(pulsoCardiaco);
  Serial.println(" BPM");

  Serial.print("Aceleración: ");
  Serial.print(aceleracion);
  Serial.println(" g");

  Serial.print("Luz: ");
  Serial.print(nivelLuz);
  Serial.println(" %");

  Serial.print("Alarma: ");
  Serial.println(alarmaActiva ? "ACTIVA" : "Inactiva");

  Serial.print("WiFi: ");
  Serial.print(WiFi.RSSI());
  Serial.println(" dBm");

  Serial.print("Memoria libre: ");
  Serial.print(ESP.getFreeHeap());
  Serial.println(" bytes");

  Serial.println("---------------------------");
}
