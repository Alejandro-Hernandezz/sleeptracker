package com.example.sleeptracker

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sleeptracker.model.SleepAnalyzer
import com.example.sleeptracker.model.SleepLevel
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SleepTracker"
    }

    // UI Elements - Sensores
    private lateinit var tvTemperatura: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvPulso: TextView
    private lateinit var tvAceleracion: TextView
    private lateinit var tvLuz: TextView
    
    // UI Elements - Sleep Quality
    private lateinit var tvSleepScore: TextView
    private lateinit var tvSleepLevel: TextView
    private lateinit var tvSleepRecommendation: TextView
    
    // UI Elements - Estado
    private lateinit var tvEstado: TextView
    private lateinit var tvUltimaActualizacion: TextView
    private lateinit var btnAlarma: MaterialButton

    // Firebase
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var refUltimos: DatabaseReference
    private lateinit var refControl: DatabaseReference

    // Variables para almacenar datos
    private var temperatura: Float = 0f
    private var humedad: Float = 0f
    private var pulso: Int = 0
    private var aceleracion: Float = 0f
    private var luz: Int = 0
    private var alarmaActiva: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()
        initializeFirebase()
    }

    /**
     * Inicializar elementos de la interfaz
     */
    private fun initializeUI() {
        // Sensores
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvHumedad = findViewById(R.id.tvHumedad)
        tvPulso = findViewById(R.id.tvPulso)
        tvAceleracion = findViewById(R.id.tvAceleracion)
        tvLuz = findViewById(R.id.tvLuz)
        
        // Sleep Quality
        tvSleepScore = findViewById(R.id.tvSleepScore)
        tvSleepLevel = findViewById(R.id.tvSleepLevel)
        tvSleepRecommendation = findViewById(R.id.tvSleepRecommendation)
        
        // Estado
        tvEstado = findViewById(R.id.tvEstado)
        tvUltimaActualizacion = findViewById(R.id.tvUltimaActualizacion)
        btnAlarma = findViewById(R.id.btnAlarma)

        // Configurar click listener del botón de alarma
        btnAlarma.setOnClickListener {
            toggleAlarma()
        }
    }

    /**
     * Inicializar Firebase Realtime Database
     */
    private fun initializeFirebase() {
        try {
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseDatabase.setPersistenceEnabled(true)

            // Referencia a la carpeta "ultimos" para datos actuales
            refUltimos = firebaseDatabase.getReference("sleep_tracker/ultimos")

            // Referencia a la carpeta "control" para controlar actuadores
            refControl = firebaseDatabase.getReference("sleep_tracker/control")

            Log.d(TAG, "Referencias Firebase configuradas:")
            Log.d(TAG, "  - ultimos: ${refUltimos.path}")
            Log.d(TAG, "  - control: ${refControl.path}")

            // Listener de prueba para ver toda la estructura
            Log.d(TAG, "Registrando listener de estructura en: sleep_tracker")
            firebaseDatabase.getReference("sleep_tracker").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "=== ESTRUCTURA COMPLETA DE sleep_tracker ===")
                        Log.d(TAG, "  Existe: ${snapshot.exists()}")
                        Log.d(TAG, "  Children: ${snapshot.childrenCount}")
                        if (snapshot.exists()) {
                            snapshot.children.forEach { child ->
                                Log.d(TAG, "    - ${child.key}:")
                                if (child.hasChildren()) {
                                    child.children.forEach { subchild ->
                                        Log.d(TAG, "        * ${subchild.key} = ${subchild.value}")
                                    }
                                } else {
                                    Log.d(TAG, "        ${child.value}")
                                }
                            }
                        } else {
                            Log.w(TAG, "  ¡El nodo sleep_tracker NO EXISTE en Firebase!")
                        }
                        Log.d(TAG, "===========================================")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error leyendo estructura: ${error.message}")
                        Log.e(TAG, "  Código: ${error.code}")
                        Log.e(TAG, "  Detalles: ${error.details}")
                    }
                }
            )

            // Escuchar cambios en tiempo real
            Log.d(TAG, "Configurando listeners de sensores...")
            setupListeners()

            // Escuchar estado de alarma
            setupAlarmListener()

            updateStatus("Conectado a Firebase", isConnected = true)
            Log.d(TAG, "Firebase inicializado correctamente")

        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando Firebase", e)
            updateStatus("Error: ${e.message}", isConnected = false)
        }
    }

    /**
     * Configurar listeners para cada sensor
     */
    private fun setupListeners() {
        Log.d(TAG, "setupListeners() - Iniciando configuración de listeners...")

        // Escuchar Temperatura
        Log.d(TAG, "Registrando listener para: sleep_tracker/ultimos/temperatura")
        refUltimos.child("temperatura").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        // Intentar leer como Number (soporta Long y Double)
                        val value = snapshot.getValue(Number::class.java)
                        if (value != null) {
                            temperatura = value.toFloat()
                            updateTemperatureUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Temperatura actualizada: $temperatura°C (raw: $value)")
                        } else {
                            Log.w(TAG, "Temperatura es null")
                        }
                    } else {
                        Log.w(TAG, "Nodo temperatura no existe")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo temperatura: ${snapshot.value}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos temperatura: ${error.message}")
            }
        })

        // Escuchar Humedad
        refUltimos.child("humedad").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val value = snapshot.getValue(Number::class.java)
                        if (value != null) {
                            humedad = value.toFloat()
                            updateHumidityUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Humedad actualizada: $humedad% (raw: $value)")
                        } else {
                            Log.w(TAG, "Humedad es null")
                        }
                    } else {
                        Log.w(TAG, "Nodo humedad no existe")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo humedad: ${snapshot.value}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos humedad: ${error.message}")
            }
        })

        // Escuchar Pulso Cardíaco
        refUltimos.child("pulso").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val value = snapshot.getValue(Number::class.java)
                        if (value != null) {
                            pulso = value.toInt()
                            updatePulseUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Pulso actualizado: $pulso BPM (raw: $value)")
                        } else {
                            Log.w(TAG, "Pulso es null")
                        }
                    } else {
                        Log.w(TAG, "Nodo pulso no existe")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo pulso: ${snapshot.value}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos pulso: ${error.message}")
            }
        })

        // Escuchar Aceleración
        refUltimos.child("aceleracion").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val value = snapshot.getValue(Number::class.java)
                        if (value != null) {
                            aceleracion = value.toFloat()
                            updateAccelerationUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Aceleración actualizada: $aceleracion g (raw: $value)")
                        } else {
                            Log.w(TAG, "Aceleración es null")
                        }
                    } else {
                        Log.w(TAG, "Nodo aceleracion no existe")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo aceleración: ${snapshot.value}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos aceleracion: ${error.message}")
            }
        })

        // Escuchar Luz
        refUltimos.child("luz").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val value = snapshot.getValue(Number::class.java)
                        if (value != null) {
                            luz = value.toInt()
                            updateLightUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Luz actualizada: $luz% (raw: $value)")
                        } else {
                            Log.w(TAG, "Luz es null")
                        }
                    } else {
                        Log.w(TAG, "Nodo luz no existe")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo luz: ${snapshot.value}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos luz: ${error.message}")
            }
        })
    }

    // ========== MÉTODOS PARA ACTUALIZAR UI ==========

    private fun updateTemperatureUI() {
        runOnUiThread {
            tvTemperatura.text = String.format("%.1f°C", temperatura)
            tvTemperatura.setTextColor(getColorForTemperature(temperatura))
        }
    }

    private fun updateHumidityUI() {
        runOnUiThread {
            tvHumedad.text = String.format("%.1f%%", humedad)
            tvHumedad.setTextColor(getColorForHumidity(humedad))
        }
    }

    private fun updatePulseUI() {
        runOnUiThread {
            tvPulso.text = String.format("%d BPM", pulso)
            tvPulso.setTextColor(getColorForPulse(pulso))
        }
    }

    private fun updateAccelerationUI() {
        runOnUiThread {
            tvAceleracion.text = String.format("%.2f g", aceleracion)
            tvAceleracion.setTextColor(getColorForAcceleration(aceleracion))
        }
    }

    private fun updateLightUI() {
        runOnUiThread {
            tvLuz.text = String.format("%d%%", luz)
            tvLuz.setTextColor(getColorForLight(luz))
        }
    }

    private fun updateLastUpdate() {
        runOnUiThread {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            tvUltimaActualizacion.text = "Última actualización: $timestamp"
        }
    }

    private fun updateStatus(message: String, isConnected: Boolean = true) {
        runOnUiThread {
            tvEstado.text = message
            val color = if (isConnected) R.color.status_connected else R.color.status_error
            tvEstado.setTextColor(ContextCompat.getColor(this, color))
        }
    }

    // ========== ANÁLISIS DE CALIDAD DE SUEÑO ==========

    private fun analyzeSleepQuality() {
        runOnUiThread {
            try {
                val quality = SleepAnalyzer.analyze(
                    temperatura, humedad, pulso, aceleracion, luz
                )

                // Actualizar score
                tvSleepScore.text = quality.score.toString()

                // Actualizar nivel con color
                val (levelText, levelColor) = when (quality.level) {
                    SleepLevel.EXCELLENT -> "Excelente" to R.color.sleep_excellent
                    SleepLevel.GOOD -> "Bueno" to R.color.sleep_good
                    SleepLevel.FAIR -> "Regular" to R.color.sleep_fair
                    SleepLevel.POOR -> "Pobre" to R.color.sleep_poor
                }
                
                tvSleepLevel.text = levelText
                tvSleepLevel.setTextColor(ContextCompat.getColor(this, levelColor))
                tvSleepScore.setTextColor(ContextCompat.getColor(this, levelColor))

                // Actualizar recomendación
                tvSleepRecommendation.text = quality.recommendation

            } catch (e: Exception) {
                Log.e(TAG, "Error analizando calidad de sueño", e)
            }
        }
    }

    // ========== MÉTODOS PARA OBTENER COLORES ==========

    private fun getColorForTemperature(temp: Float): Int {
        return ContextCompat.getColor(this, when {
            temp < 16f -> R.color.temp_cold
            temp < 22f -> R.color.temp_optimal
            temp < 26f -> R.color.temp_warm
            else -> R.color.temp_hot
        })
    }

    private fun getColorForHumidity(hum: Float): Int {
        return ContextCompat.getColor(this, when {
            hum < 30f -> R.color.humidity_dry
            hum < 70f -> R.color.humidity_optimal
            else -> R.color.humidity_wet
        })
    }

    private fun getColorForPulse(bpm: Int): Int {
        return ContextCompat.getColor(this, when {
            bpm < 55 -> R.color.heart_low
            bpm < 85 -> R.color.heart_optimal
            bpm < 110 -> R.color.heart_elevated
            else -> R.color.heart_high
        })
    }

    private fun getColorForAcceleration(acel: Float): Int {
        return ContextCompat.getColor(this, when {
            acel < 0.5f -> R.color.movement_still
            acel < 1.0f -> R.color.movement_low
            acel < 1.5f -> R.color.movement_moderate
            else -> R.color.movement_high
        })
    }

    private fun getColorForLight(luz: Int): Int {
        return ContextCompat.getColor(this, when {
            luz < 20 -> R.color.light_dark
            luz < 50 -> R.color.light_dim
            luz < 80 -> R.color.light_moderate
            else -> R.color.light_bright
        })
    }

    // ========== CONTROL DE ALARMA ==========

    private fun setupAlarmListener() {
        refControl.child("alarma").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        Log.d(TAG, "Alarma raw value: ${snapshot.value}, type: ${snapshot.value?.javaClass?.simpleName}")

                        // Firebase puede guardar la alarma como Boolean o como número
                        alarmaActiva = when (val value = snapshot.value) {
                            is Boolean -> value
                            is Long -> value != 0L
                            is Int -> value != 0
                            is Number -> value.toInt() != 0
                            is String -> value.equals("true", ignoreCase = true) || value == "1"
                            else -> {
                                Log.w(TAG, "Tipo de alarma desconocido: ${value?.javaClass?.simpleName}")
                                false
                            }
                        }

                        updateAlarmUI()
                        Log.d(TAG, "Estado de alarma: ${if (alarmaActiva) "ACTIVA" else "INACTIVA"}")
                    } else {
                        Log.w(TAG, "Nodo alarma no existe")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo estado de alarma: ${snapshot.value}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos en alarma: ${error.message}")
            }
        })
    }

    private fun toggleAlarma() {
        alarmaActiva = !alarmaActiva

        // Enviar nuevo estado a Firebase como número (0 o 1)
        val valorAlarma = if (alarmaActiva) 1 else 0
        refControl.child("alarma").setValue(valorAlarma)
            .addOnSuccessListener {
                Log.d(TAG, "Estado de alarma actualizado: $alarmaActiva (valor: $valorAlarma)")
                updateAlarmUI()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error actualizando alarma", e)
                // Revertir el cambio local si falla
                alarmaActiva = !alarmaActiva
            }
    }

    private fun updateAlarmUI() {
        runOnUiThread {
            if (alarmaActiva) {
                btnAlarma.text = "Desactivar Alarma"
                btnAlarma.setBackgroundColor(ContextCompat.getColor(this, R.color.alarm_active))
            } else {
                btnAlarma.text = "Activar Alarma"
                btnAlarma.setBackgroundColor(ContextCompat.getColor(this, R.color.alarm_inactive))
            }
        }
    }
}
