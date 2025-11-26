package com.example.sleeptracker

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SleepTracker"
    }

    // UI Elements
    private lateinit var tvTemperatura: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvPulso: TextView
    private lateinit var tvAceleracion: TextView
    private lateinit var tvLuz: TextView
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
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvHumedad = findViewById(R.id.tvHumedad)
        tvPulso = findViewById(R.id.tvPulso)
        tvAceleracion = findViewById(R.id.tvAceleracion)
        tvLuz = findViewById(R.id.tvLuz)
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

            updateStatus("Conectado a Firebase")
            Log.d(TAG, "Firebase inicializado correctamente")

        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando Firebase", e)
            updateStatus("Error: ${e.message}")
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
                        val temp = snapshot.child("valor").getValue(Double::class.java)
                        if (temp != null) {
                            temperatura = temp.toFloat()
                            updateTemperatureUI()
                            updateLastUpdate()
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
                        val hum = snapshot.child("valor").getValue(Double::class.java)
                        if (hum != null) {
                            humedad = hum.toFloat()
                            updateHumidityUI()
                            updateLastUpdate()
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
                        val bpm = snapshot.child("valor").getValue(Long::class.java)
                        if (bpm != null) {
                            pulso = bpm.toInt()
                            updatePulseUI()
                            updateLastUpdate()
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
                        val acel = snapshot.child("valor").getValue(Double::class.java)
                        if (acel != null) {
                            aceleracion = acel.toFloat()
                            updateAccelerationUI()
                            updateLastUpdate()
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
                        val luzValue = snapshot.child("valor").getValue(Long::class.java)
                        if (luzValue != null) {
                            luz = luzValue.toInt()
                            updateLightUI()
                            updateLastUpdate()
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

    private fun updateStatus(message: String) {
        runOnUiThread {
            tvEstado.text = message
        }
    }

    // ========== MÉTODOS PARA OBTENER COLORES ==========

    /**
     * Color dinámico para temperatura
     * Azul (frío) → Verde (normal) → Rojo (caliente)
     */
    private fun getColorForTemperature(temp: Float): Int {
        return when {
            temp < 18 -> 0xFF0080FF.toInt() // Azul
            temp < 25 -> 0xFF00CC00.toInt() // Verde
            else -> 0xFFFF0000.toInt()      // Rojo
        }
    }

    /**
     * Color dinámico para humedad
     * Rojo (seco) → Verde (normal) → Azul (húmedo)
     */
    private fun getColorForHumidity(hum: Float): Int {
        return when {
            hum < 30 -> 0xFFFF0000.toInt()  // Rojo
            hum < 70 -> 0xFF00CC00.toInt()  // Verde
            else -> 0xFF0080FF.toInt()      // Azul
        }
    }

    /**
     * Color dinámico para pulso
     * Azul (bajo) → Verde (normal) → Amarillo (elevado) → Rojo (muy elevado)
     */
    private fun getColorForPulse(bpm: Int): Int {
        return when {
            bpm < 60 -> 0xFF0080FF.toInt()  // Azul
            bpm < 100 -> 0xFF00CC00.toInt() // Verde
            bpm < 130 -> 0xFFFFCC00.toInt() // Amarillo
            else -> 0xFFFF0000.toInt()      // Rojo
        }
    }

    /**
     * Color dinámico para aceleración
     * Verde (quieto) → Amarillo (movimiento) → Rojo (mucho movimiento)
     */
    private fun getColorForAcceleration(acel: Float): Int {
        return when {
            acel < 1.0 -> 0xFF00CC00.toInt() // Verde
            acel < 1.5 -> 0xFFFFCC00.toInt() // Amarillo
            else -> 0xFFFF0000.toInt()       // Rojo
        }
    }

    /**
     * Color dinámico para luz
     * Azul (oscuro) → Verde (normal) → Amarillo (claro)
     */
    private fun getColorForLight(luz: Int): Int {
        return when {
            luz < 30 -> 0xFF0080FF.toInt()  // Azul
            luz < 70 -> 0xFF00CC00.toInt()  // Verde
            else -> 0xFFFFCC00.toInt()      // Amarillo
        }
    }

    // ========== CONTROL DE ALARMA ==========

    /**
     * Escuchar cambios en el estado de la alarma desde Firebase
     */
    private fun setupAlarmListener() {
        refControl.child("alarma").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        alarmaActiva = snapshot.getValue(Boolean::class.java) ?: false
                        updateAlarmUI()
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

    /**
     * Alternar estado de la alarma
     */
    private fun toggleAlarma() {
        alarmaActiva = !alarmaActiva

        // Enviar nuevo estado a Firebase
        refControl.child("alarma").setValue(alarmaActiva)
            .addOnSuccessListener {
                Log.d(TAG, "Estado de alarma actualizado: $alarmaActiva")
                updateAlarmUI()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error actualizando alarma", e)
                // Revertir el cambio local si falla
                alarmaActiva = !alarmaActiva
            }
    }

    /**
     * Actualizar UI del botón de alarma
     */
    private fun updateAlarmUI() {
        runOnUiThread {
            if (alarmaActiva) {
                btnAlarma.text = "🔕 Desactivar Alarma"
                btnAlarma.setBackgroundColor(0xFFFF0000.toInt()) // Rojo
            } else {
                btnAlarma.text = "🔔 Activar Alarma"
                btnAlarma.setBackgroundColor(0xFF00CC88.toInt()) // Verde/Teal
            }
        }
    }
}