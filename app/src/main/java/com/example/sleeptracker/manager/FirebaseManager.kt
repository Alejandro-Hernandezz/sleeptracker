
package com.example.sleeptracker.manager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sleeptracker.model.*
import com.google.firebase.database.*

/**
 * Manager para manejar toda la lógica de Firebase
 */
class FirebaseManager {

    companion object {
        private const val TAG = "FirebaseManager"
        private const val BASE_PATH = "sleep_tracker"
        private const val ULTIMOS_PATH = "$BASE_PATH/ultimos"
        private const val SENSORES_PATH = "$BASE_PATH/sensores"
    }

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val refUltimos: DatabaseReference = database.getReference(ULTIMOS_PATH)

    // LiveData para observar cambios en los sensores
    private val _temperatureLiveData = MutableLiveData<SensorData>()
    val temperatureLiveData: LiveData<SensorData> = _temperatureLiveData

    private val _humidadLiveData = MutableLiveData<SensorData>()
    val humidadLiveData: LiveData<SensorData> = _humidadLiveData

    private val _pulsoLiveData = MutableLiveData<SensorData>()
    val pulsoLiveData: LiveData<SensorData> = _pulsoLiveData

    private val _aceleracionLiveData = MutableLiveData<SensorData>()
    val aceleracionLiveData: LiveData<SensorData> = _aceleracionLiveData

    private val _luzLiveData = MutableLiveData<SensorData>()
    val luzLiveData: LiveData<SensorData> = _luzLiveData

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _healthStatus = MutableLiveData<HealthStatus>()
    val healthStatus: LiveData<HealthStatus> = _healthStatus

    init {
        // Configurar persistencia local
        try {
            database.setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al habilitar persistencia", e)
        }
    }

    /**
     * Inicializar listeners para todos los sensores
     */
    fun startListening() {
        _connectionState.value = ConnectionState.CONNECTING

        try {
            setupTemperatureListener()
            setupHumidityListener()
            setupPulseListener()
            setupAccelerationListener()
            setupLightListener()

            _connectionState.value = ConnectionState.CONNECTED
            Log.d(TAG, "Listeners iniciados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando listeners", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }

    /**
     * Listener para temperatura
     */
    private fun setupTemperatureListener() {
        refUltimos.child("temperatura").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val valor = snapshot.child("valor").getValue(Double::class.java)
                        val unidad = snapshot.child("unidad").getValue(String::class.java)
                        if (valor != null) {
                            val sensorData = SensorData(valor, unidad ?: "°C")
                            _temperatureLiveData.postValue(sensorData)
                            updateHealthStatus()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo temperatura", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error temperatura: ${error.message}")
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        })
    }

    /**
     * Listener para humedad
     */
    private fun setupHumidityListener() {
        refUltimos.child("humedad").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val valor = snapshot.child("valor").getValue(Double::class.java)
                        val unidad = snapshot.child("unidad").getValue(String::class.java)
                        if (valor != null) {
                            val sensorData = SensorData(valor, unidad ?: "%")
                            _humidadLiveData.postValue(sensorData)
                            updateHealthStatus()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo humedad", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error humedad: ${error.message}")
            }
        })
    }

    /**
     * Listener para pulso
     */
    private fun setupPulseListener() {
        refUltimos.child("pulso").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val valor = snapshot.child("valor").getValue(Long::class.java)
                        val unidad = snapshot.child("unidad").getValue(String::class.java)
                        if (valor != null) {
                            val sensorData = SensorData(valor.toDouble(), unidad ?: "BPM")
                            _pulsoLiveData.postValue(sensorData)
                            updateHealthStatus()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo pulso", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error pulso: ${error.message}")
            }
        })
    }

    /**
     * Listener para aceleración
     */
    private fun setupAccelerationListener() {
        refUltimos.child("aceleracion").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val valor = snapshot.child("valor").getValue(Double::class.java)
                        val unidad = snapshot.child("unidad").getValue(String::class.java)
                        if (valor != null) {
                            val sensorData = SensorData(valor, unidad ?: "g")
                            _aceleracionLiveData.postValue(sensorData)
                            updateHealthStatus()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo aceleración", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error aceleración: ${error.message}")
            }
        })
    }

    /**
     * Listener para luz
     */
    private fun setupLightListener() {
        refUltimos.child("luz").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val valor = snapshot.child("valor").getValue(Long::class.java)
                        val unidad = snapshot.child("unidad").getValue(String::class.java)
                        if (valor != null) {
                            val sensorData = SensorData(valor.toDouble(), unidad ?: "%")
                            _luzLiveData.postValue(sensorData)
                            updateHealthStatus()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo luz", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error luz: ${error.message}")
            }
        })
    }

    /**
     * Actualizar estado de salud basado en los sensores
     */
    private fun updateHealthStatus() {
        val temp = _temperatureLiveData.value?.valor?.toFloat() ?: 0f
        val pulso = _pulsoLiveData.value?.valor?.toFloat() ?: 0f
        val acel = _aceleracionLiveData.value?.valor?.toFloat() ?: 0f
        val luz = _luzLiveData.value?.valor?.toInt() ?: 0

        val tempStatus = when {
            temp < 18 -> "Muy Frío"
            temp < 22 -> "Frío"
            temp < 25 -> "Normal"
            else -> "Cálido"
        }

        val heartStatus = when {
            pulso < 60 -> "Bajo"
            pulso < 100 -> "Normal"
            pulso < 120 -> "Elevado"
            else -> "Muy Elevado"
        }

        val moveStatus = when {
            acel < 0.5 -> "Quieto"
            acel < 1.0 -> "Poco movimiento"
            acel < 1.5 -> "Movimiento"
            else -> "Mucho movimiento"
        }

        val lightStatus = when {
            luz < 20 -> "Muy Oscuro"
            luz < 50 -> "Oscuro"
            luz < 80 -> "Normal"
            else -> "Muy Claro"
        }

        val overall = when {
            temp in 18f..25f && pulso in 60f..100f && acel < 1.5f -> "Saludable"
            else -> "Revisar valores"
        }

        _healthStatus.postValue(
            HealthStatus(
                temperature = tempStatus,
                heartRate = heartStatus,
                movement = moveStatus,
                ambientLight = lightStatus,
                overall = overall
            )
        )
    }

    /**
     * Detener listeners
     */
    fun stopListening() {
        _connectionState.value = ConnectionState.DISCONNECTED
        Log.d(TAG, "Listeners detenidos")
    }
}