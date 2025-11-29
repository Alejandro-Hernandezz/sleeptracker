/*
 * Archivo de configuración para Sleep Tracker IoT - NodeMCU
 *
 * IMPORTANTE: Antes de cargar el código al NodeMCU:
 * 1. Copia este archivo a config.h
 * 2. Edita los valores con tu información real
 * 3. NO compartas este archivo con tus credenciales reales
 */

#ifndef CONFIG_H
#define CONFIG_H

// ============= CONFIGURACIÓN WIFI =============
// Reemplaza con el nombre de tu red WiFi
#define WIFI_SSID "TU_NOMBRE_WIFI"

// Reemplaza con la contraseña de tu red WiFi
#define WIFI_PASSWORD "TU_PASSWORD_WIFI"

// ============= CONFIGURACIÓN FIREBASE =============
// Reemplaza con tu URL de Firebase (SIN https://)
// Ejemplo: "sleeptracker-12345.firebaseio.com"
#define FIREBASE_HOST "tu-proyecto.firebaseio.com"

// Reemplaza con tu token de autenticación de Firebase
// Lo puedes encontrar en: Firebase Console > Project Settings > Service Accounts > Database Secrets
#define FIREBASE_AUTH "tu-token-secreto-aqui"

// ============= CONFIGURACIÓN DE SENSORES =============
// Intervalo de lectura de sensores en milisegundos
#define SENSOR_READ_INTERVAL 2000  // 2 segundos

// ============= PINES DE CONEXIÓN NodeMCU =============
// IMPORTANTE: NodeMCU usa nomenclatura D0-D8, no GPIO directamente

// DHT11
#define DHTPIN D4        // GPIO2
#define DHTTYPE DHT11

// LDR (Sensor de luz)
#define LDR_PIN A0       // Pin analógico único

// Actuadores
#define LED_PIN D3       // GPIO0
#define BUZZER_PIN D8    // GPIO15

// I2C (para ADXL345 y MAX30102)
// SDA: D2 (GPIO4)
// SCL: D1 (GPIO5)
// Estos se configuran automáticamente en Wire.begin(D2, D1)

// ============= TABLA DE PINES NodeMCU =============
/*
 * Pin Label | GPIO | Función
 * ----------|------|---------------------------
 * D0        | 16   | Wake from Deep Sleep
 * D1        | 5    | SCL (I2C)
 * D2        | 4    | SDA (I2C)
 * D3        | 0    | Flash (LED integrado)
 * D4        | 2    | Built-in LED
 * D5        | 14   | SPI CLK
 * D6        | 12   | SPI MISO
 * D7        | 13   | SPI MOSI
 * D8        | 15   | SPI CS
 * A0        | ADC  | Analog Input (0-1V max!)
 */

#endif
