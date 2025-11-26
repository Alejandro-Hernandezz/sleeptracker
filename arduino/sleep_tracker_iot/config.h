/*
 * Archivo de configuración para Sleep Tracker IoT
 *
 * IMPORTANTE: Antes de cargar el código al Arduino:
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
// Reemplaza con tu URL de Firebase
// Ejemplo: "sleeptracker-12345.firebaseio.com"
#define FIREBASE_HOST "tu-proyecto.firebaseio.com"

// Reemplaza con tu token de autenticación de Firebase
// Lo puedes encontrar en: Firebase Console > Project Settings > Service Accounts > Database Secrets
#define FIREBASE_AUTH "tu-token-secreto-aqui"

// ============= CONFIGURACIÓN DE SENSORES =============
// Intervalo de lectura de sensores en milisegundos
#define SENSOR_READ_INTERVAL 2000  // 2 segundos

// Intervalo de envío a Firebase en milisegundos
#define FIREBASE_SEND_INTERVAL 5000  // 5 segundos

// ============= PINES DE CONEXIÓN =============
// DHT11
#define DHTPIN 4
#define DHTTYPE DHT11

// LDR (Sensor de luz)
#define LDR_PIN 34

// Actuadores
#define LED_PIN 2
#define BUZZER_PIN 5

// I2C (para ADXL345 y MAX30102)
// SDA: Pin 21
// SCL: Pin 22

#endif
