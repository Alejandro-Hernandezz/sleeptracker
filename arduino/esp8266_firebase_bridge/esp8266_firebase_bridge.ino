/*
 * ESP8266 - SISTEMA IoT: RASTREADOR DE SUEÑO
 * Función: Puente entre Arduino y Firebase
 *
 * LIBRERÍAS NECESARIAS:
 * - ESP8266WiFi (incluida)
 * - FirebaseESP8266 (instalar desde Library Manager)
 *   Buscar: "Firebase Arduino Client Library for ESP8266" by Mobizt
 *
 * CORRECCIONES APLICADAS:
 * - Firebase Host sin https:// ni /
 * - Delays entre operaciones Firebase
 * - Buffers SSL optimizados
 * - Mejor manejo de errores
 */

#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include <SoftwareSerial.h>
#include <time.h>

// ========== CONFIGURACIÓN WiFi ==========
#define WIFI_SSID "Mclovin"
#define WIFI_PASSWORD "123456788"

// ========== CONFIGURACIÓN FIREBASE ==========
// ⚠️ IMPORTANTE: Sin https:// y sin / al final
#define FIREBASE_HOST "sleeptracker-c3bc1-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "tDQfFUKRp2VGAntinHQLZbnX9U8fVSDhsbkgezZT"

// Objetos Firebase
FirebaseData firebaseData;
FirebaseConfig firebaseConfig;
FirebaseAuth firebaseAuth;

// ========== CONFIGURACIÓN SERIAL ==========
#define RX_PIN D7
#define TX_PIN D8
SoftwareSerial arduinoSerial(RX_PIN, TX_PIN);

// ========== VARIABLES GLOBALES ==========
struct SensorData {
  float aceleracion;
  float temperatura;
  float humedad;
  int pulso;
  int luz;
  unsigned long timestamp;
};

SensorData datosActuales;
bool datosNuevos = false;
unsigned long ultimaLecturaLED = 0;
const long intervaloLecturaLED = 5000; // Aumentado a 5 segundos
unsigned long ultimoEnvioFirebase = 0;
const long intervaloEnvioFirebase = 3000; // Enviar cada 3 segundos como mínimo

// ==========================================
// ==========     SETUP PRINCIPAL     =======
// ==========================================
void setup() {
  Serial.begin(115200);
  arduinoSerial.begin(9600);

  delay(2000);

  Serial.println("\n\n=== ESP8266 - FIREBASE BRIDGE (v2.0) ===");
  Serial.println("Sleep Tracker IoT");

  connectToWiFi();
  sincronizarTiempoNTP();
  setupFirebase();

  Serial.println("✓ Sistema listo");
  Serial.println("Esperando datos del Arduino...\n");
}

// ==========================================
// ==========         LOOP           ========
// ==========================================
void loop() {
  // Verificar conexión WiFi
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("⚠ WiFi desconectado, reconectando...");
    connectToWiFi();
  }

  // Leer datos del Arduino
  leerDatosArduino();

  // Enviar datos solo si pasó el intervalo mínimo
  unsigned long tiempoActual = millis();
  if (datosNuevos && (tiempoActual - ultimoEnvioFirebase >= intervaloEnvioFirebase)) {
    datosNuevos = false;
    ultimoEnvioFirebase = tiempoActual;
    enviarDatosFirebase();
  }

  // Leer estado LED de Firebase cada 5 segundos
  if (tiempoActual - ultimaLecturaLED >= intervaloLecturaLED) {
    ultimaLecturaLED = tiempoActual;
    leerEstadoLEDDesdeFirebase();
  }

  delay(100); // Pequeño delay para estabilidad
}

// ==========================================
// ========== FUNCIONES PRINCIPALES =========
// ==========================================

// ------ WIFI ------
void connectToWiFi() {
  Serial.print("Conectando a WiFi: ");
  Serial.println(WIFI_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int intentos = 0;
  while (WiFi.status() != WL_CONNECTED && intentos < 30) {
    delay(500);
    Serial.print(".");
    intentos++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✓ WiFi conectado");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
    Serial.print("Señal: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
  } else {
    Serial.println("\n✗ Error WiFi - Reiniciando...");
    delay(3000);
    ESP.restart();
  }
}

// ------ TIEMPO NTP ------
void sincronizarTiempoNTP() {
  Serial.print("Sincronizando hora NTP...");

  configTime(0, 0, "pool.ntp.org", "time.nist.gov");

  int intentos = 0;
  while (time(nullptr) < 100000 && intentos < 20) {
    Serial.print(".");
    delay(500);
    intentos++;
  }

  if (time(nullptr) >= 100000) {
    Serial.println(" ✓");
  } else {
    Serial.println(" ⚠ Timeout (continuando sin NTP)");
  }
}

// ------ FIREBASE ------
void setupFirebase() {
  Serial.println("Configurando Firebase...");

  // Configuración básica
  firebaseConfig.host = FIREBASE_HOST;
  firebaseConfig.signer.tokens.legacy_token = FIREBASE_AUTH;

  // Configuración de timeouts (IMPORTANTE para evitar SSL timeout)
  firebaseConfig.timeout.serverResponse = 10 * 1000; // 10 segundos
  firebaseConfig.timeout.socketConnection = 10 * 1000; // 10 segundos

  // Buffers SSL optimizados para ESP8266
  // Valores más conservadores para evitar errores de memoria
  firebaseData.setBSSLBufferSize(1024, 512);
  firebaseData.setResponseSize(1024);

  // Iniciar Firebase
  Firebase.begin(&firebaseConfig, &firebaseAuth);
  Firebase.reconnectWiFi(true);

  // Pequeño delay después de inicializar
  delay(1000);

  Serial.println("✓ Firebase configurado");

  // Escribir estado inicial (con manejo de errores)
  if (Firebase.setString(firebaseData, "/sleep_tracker/status", "online")) {
    Serial.println("✓ Estado inicial actualizado");
  } else {
    Serial.print("⚠ Error inicial: ");
    Serial.println(firebaseData.errorReason());
  }

  delay(500); // Delay entre operaciones
}

// ------ LECTURA DESDE ARDUINO ------
void leerDatosArduino() {
  if (arduinoSerial.available()) {
    String data = arduinoSerial.readStringUntil('\n');
    data.trim();

    if (data.length() > 0 && data.indexOf("ACEL:") != -1) {
      Serial.print("[Arduino] ");
      Serial.println(data);
      parsearDatos(data);
    }
  }
}

// ------ PARSEAR DATOS ------
void parsearDatos(String data) {
  int idx = data.indexOf("ACEL:");
  if (idx != -1) {
    int endIdx = data.indexOf(",", idx);
    datosActuales.aceleracion = data.substring(idx + 5, endIdx).toFloat();
  }

  idx = data.indexOf("TEMP:");
  if (idx != -1) {
    int endIdx = data.indexOf(",", idx);
    datosActuales.temperatura = data.substring(idx + 5, endIdx).toFloat();
  }

  idx = data.indexOf("HUM:");
  if (idx != -1) {
    int endIdx = data.indexOf(",", idx);
    datosActuales.humedad = data.substring(idx + 4, endIdx).toFloat();
  }

  idx = data.indexOf("BPM:");
  if (idx != -1) {
    int endIdx = data.indexOf(",", idx);
    if (endIdx == -1) endIdx = data.length();
    datosActuales.pulso = data.substring(idx + 4, endIdx).toInt();
  }

  idx = data.indexOf("LUZ:");
  if (idx != -1) {
    datosActuales.luz = data.substring(idx + 4).toInt();
  }

  datosActuales.timestamp = millis();
  datosNuevos = true;
}

// ------ ENVÍO DE DATOS A FIREBASE (OPTIMIZADO) ------
void enviarDatosFirebase() {
  Serial.println("[Firebase] Enviando datos...");

  // Crear JSON para enviar todo de una vez (más eficiente)
  FirebaseJson json;

  json.set("temperatura", datosActuales.temperatura);
  json.set("humedad", datosActuales.humedad);
  json.set("pulso", datosActuales.pulso);
  json.set("luz", datosActuales.luz);
  json.set("aceleracion", datosActuales.aceleracion);
  json.set("timestamp", datosActuales.timestamp);

  // Enviar todo el JSON de una vez
  if (Firebase.setJSON(firebaseData, "/sleep_tracker/ultimos", json)) {
    Serial.println("✓ Datos enviados exitosamente");
    Serial.print("  Temp: ");
    Serial.print(datosActuales.temperatura);
    Serial.print("°C | Hum: ");
    Serial.print(datosActuales.humedad);
    Serial.print("% | BPM: ");
    Serial.print(datosActuales.pulso);
    Serial.print(" | Luz: ");
    Serial.print(datosActuales.luz);
    Serial.println("%");
  } else {
    Serial.print("✗ Error Firebase: ");
    Serial.println(firebaseData.errorReason());

    // Si hay error, esperar antes del siguiente intento
    delay(2000);
  }

  delay(200); // Delay entre operaciones Firebase

  // Alertas (solo si es necesario)
  bool alertaTemp = (datosActuales.temperatura < 18.0 || datosActuales.temperatura > 30.0);
  bool alertaBPM = (datosActuales.pulso > 0 && (datosActuales.pulso < 50 || datosActuales.pulso > 110));

  if (alertaTemp || alertaBPM) {
    FirebaseJson alertasJson;
    alertasJson.set("temperatura_alerta", alertaTemp);
    alertasJson.set("pulso_alerta", alertaBPM);

    if (alertaTemp) {
      alertasJson.set("ultima_alerta", "temperatura");
    } else if (alertaBPM) {
      alertasJson.set("ultima_alerta", "pulso");
    }

    Firebase.setJSON(firebaseData, "/sleep_tracker/alertas", alertasJson);
    delay(200);
  }

  // Mostrar memoria libre
  Serial.print("  Memoria libre: ");
  Serial.print(ESP.getFreeHeap());
  Serial.println(" bytes");
}

// ------ LECTURA LED (SIMPLIFICADO) ------
void leerEstadoLEDDesdeFirebase() {
  if (Firebase.getInt(firebaseData, "/sleep_tracker/control/alarma")) {
    int estadoLED = firebaseData.intData();

    // Enviar comando al Arduino
    arduinoSerial.print("LED_VERDE:");
    arduinoSerial.println(estadoLED);

    Serial.print("[Firebase] LED Verde: ");
    Serial.println(estadoLED == 1 ? "ON" : "OFF");
  } else {
    Serial.print("⚠ Error leyendo LED: ");
    Serial.println(firebaseData.errorReason());
  }
}
