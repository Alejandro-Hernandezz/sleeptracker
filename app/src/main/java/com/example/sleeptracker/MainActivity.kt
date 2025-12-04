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

            // Escuchar cambios en tiempo real
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
        // Escuchar Temperatura
        refUltimos.child("temperatura").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val temp = snapshot.getValue(Double::class.java)
                        if (temp != null) {
                            temperatura = temp.toFloat()
                            updateTemperatureUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Temperatura actualizada: $temperatura°C")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo temperatura", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos: ${error.message}")
            }
        })

        // Escuchar Humedad
        refUltimos.child("humedad").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val hum = snapshot.getValue(Double::class.java)
                        if (hum != null) {
                            humedad = hum.toFloat()
                            updateHumidityUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Humedad actualizada: $humedad%")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo humedad", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos: ${error.message}")
            }
        })

        // Escuchar Pulso Cardíaco
        refUltimos.child("pulso").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val bpm = snapshot.getValue(Long::class.java)
                        if (bpm != null) {
                            pulso = bpm.toInt()
                            updatePulseUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Pulso actualizado: $pulso BPM")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo pulso", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos: ${error.message}")
            }
        })

        // Escuchar Aceleración
        refUltimos.child("aceleracion").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val acel = snapshot.getValue(Double::class.java)
                        if (acel != null) {
                            aceleracion = acel.toFloat()
                            updateAccelerationUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Aceleración actualizada: $aceleracion g")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo aceleración", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos: ${error.message}")
            }
        })

        // Escuchar Luz
        refUltimos.child("luz").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val luzValue = snapshot.getValue(Long::class.java)
                        if (luzValue != null) {
                            luz = luzValue.toInt()
                            updateLightUI()
                            updateLastUpdate()
                            analyzeSleepQuality()
                            Log.d(TAG, "Luz actualizada: $luz%")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo luz", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error de base de datos: ${error.message}")
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
                        // Firebase guarda la alarma como número (0 o 1)
                        val alarmaValue = snapshot.getValue(Long::class.java) ?: 0L
                        alarmaActiva = alarmaValue != 0L
                        updateAlarmUI()
                        Log.d(TAG, "Estado de alarma: ${if (alarmaActiva) "ACTIVA" else "INACTIVA"}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo estado de alarma", e)
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
