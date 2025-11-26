# Capturas de Pantalla Requeridas para el Proyecto

## 📸 Lista de Capturas Necesarias

### 1. Tinkercad (`/docs/tinkercad/`)

- [ ] `circuito_completo.png` - Vista general del montaje en Tinkercad
- [ ] `conexiones_sensores.png` - Zoom a las conexiones de los sensores
- [ ] `conexiones_actuadores.png` - Zoom a LED y buzzer
- [ ] `simulacion_funcionando.png` - Serial Monitor mostrando datos
- [ ] `lista_componentes.png` - Panel de componentes utilizado

**Cómo obtener**:
1. Abre tu proyecto en Tinkercad
2. Haz clic en el botón de cámara o usa Print Screen
3. Guarda las imágenes en esta carpeta

---

### 2. Firebase Console (`/docs/firebase/`)

- [ ] `firebase_console_datos.png` - Vista de `sleep_tracker/ultimos` con datos actualizándose
- [ ] `firebase_console_control.png` - Vista del nodo `control/alarma`
- [ ] `firebase_reglas.png` - Captura de las reglas de seguridad
- [ ] `firebase_estructura.png` - Árbol completo de la base de datos
- [ ] `firebase_tiempo_real.png` - Captura mostrando actualización en vivo (timestamp cambiando)

**Cómo obtener**:
1. Ve a https://console.firebase.google.com
2. Selecciona tu proyecto
3. Ve a Realtime Database
4. Expande los nodos y toma capturas
5. Para mostrar tiempo real: deja el Arduino funcionando y captura cuando los valores cambien

---

### 3. App Android (`/docs/screenshots/`)

- [ ] `app_splash.png` - Pantalla de inicio (si existe)
- [ ] `app_conectando.png` - Estado "Conectando a Firebase"
- [ ] `app_conectado.png` - Estado "Conectado a Firebase"
- [ ] `app_sensores_cero.png` - Estado inicial con valores en 0
- [ ] `app_sensores_activos.png` - Todos los sensores mostrando datos reales
- [ ] `app_temperatura_fria.png` - Temperatura en azul (<18°C)
- [ ] `app_temperatura_normal.png` - Temperatura en verde (18-25°C)
- [ ] `app_temperatura_caliente.png` - Temperatura en rojo (>25°C)
- [ ] `app_alarma_inactiva.png` - Botón verde "🔔 Activar Alarma"
- [ ] `app_alarma_activa.png` - Botón rojo "🔕 Desactivar Alarma"
- [ ] `app_colores_dinamicos.png` - Varios sensores con diferentes colores

**Cómo obtener**:
1. Ejecuta la app en tu dispositivo Android
2. Usa los botones de captura de pantalla del dispositivo:
   - Samsung: Power + Volume Down
   - Google Pixel: Power + Volume Down
   - Otros: Power + Volume Down (generalmente)
3. Transfiere las imágenes a tu PC
4. Guarda en esta carpeta

---

### 4. Hardware Físico (`/docs/hardware/`)

- [ ] `prototipo_general.jpg` - Vista general del montaje en protoboard
- [ ] `prototipo_esp32.jpg` - Zoom al ESP32
- [ ] `prototipo_sensores.jpg` - Sensores conectados
- [ ] `prototipo_dht11.jpg` - Sensor DHT11
- [ ] `prototipo_max30102.jpg` - Sensor MAX30102 (si está visible)
- [ ] `prototipo_adxl345.jpg` - Acelerómetro ADXL345
- [ ] `prototipo_ldr.jpg` - Fotoresistor LDR con resistencia
- [ ] `prototipo_led_buzzer.jpg` - LED y buzzer
- [ ] `prototipo_funcionando.jpg` - LED encendido o monitor serial visible
- [ ] `prototipo_conexiones.jpg` - Vista de los cables de conexión

**Cómo obtener**:
1. Usa la cámara de tu smartphone
2. Asegúrate de tener buena iluminación
3. Toma fotos desde diferentes ángulos
4. Enfoca bien cada componente
5. Transfiere a tu PC y guarda aquí

---

### 5. Serial Monitor de Arduino (`/docs/arduino/`)

- [ ] `serial_monitor_inicio.png` - Mensajes de inicialización
- [ ] `serial_monitor_wifi.png` - Conexión WiFi exitosa
- [ ] `serial_monitor_firebase.png` - Conexión Firebase exitosa
- [ ] `serial_monitor_datos.png` - Datos de sensores siendo leídos
- [ ] `serial_monitor_alarma_on.png` - Mensaje cuando se activa alarma
- [ ] `serial_monitor_alarma_off.png` - Mensaje cuando se desactiva alarma

**Cómo obtener**:
1. Abre Arduino IDE
2. Conecta el ESP32
3. Abre Tools > Serial Monitor
4. Selecciona 115200 baud
5. Deja que se conecte y muestre datos
6. Usa Print Screen para capturar
7. Guarda las imágenes aquí

---

## 🎯 Checklist de Capturas Completas

### Para el Informe PDF necesitas mínimo:
- [x] 2 capturas de Tinkercad (circuito + simulación)
- [x] 2 capturas de Firebase (datos + control)
- [x] 4 capturas de la app (sensores + alarma ON/OFF + colores)
- [x] 3 fotos del hardware (general + detalle + funcionando)
- [x] 1 captura del Serial Monitor

### Para una presentación completa:
- [x] Todas las capturas listadas arriba

---

## 📝 Notas

- Todas las imágenes deben ser claras y legibles
- Tamaño recomendado: mínimo 1280x720 px
- Formato: PNG para capturas de pantalla, JPG para fotos
- Nombra los archivos exactamente como se indica
- No edites las imágenes (excepto recortar si es necesario)

---

## 📋 Template para Documentar cada Captura

Cuando agregues una captura, documenta:

```markdown
### Nombre de archivo: `app_sensores_activos.png`
- **Descripción**: Pantalla principal mostrando todos los sensores con datos reales
- **Fecha de captura**: 2024-11-26
- **Valores mostrados**:
  - Temperatura: 22.5°C (verde)
  - Humedad: 55% (verde)
  - Pulso: 72 BPM (verde)
  - Aceleración: 0.98g (verde)
  - Luz: 45% (verde)
- **Observaciones**: Todos los valores están en rango normal
```

---

Actualiza este archivo marcando las casillas [x] cuando captures cada imagen.
