package com.example.sleeptracker.model

/**
 * Modelo de datos para un sensor individual
 */
data class SensorData(
    val valor: Double = 0.0,
    val unidad: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this(0.0, "", System.currentTimeMillis())
}

/**
 * Modelo consolidado de todos los sensores
 */
data class AllSensorsData(
    val aceleracion: Float = 0f,
    val temperatura: Float = 0f,
    val humedad: Float = 0f,
    val pulso: Int = 0,
    val luz: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this(0f, 0f, 0f, 0, 0, System.currentTimeMillis())
}

/**
 * Clase para representar un rango de valores aceptables
 */
data class SensorRange(
    val min: Float,
    val max: Float,
    val optimal: Float
) {
    fun isInRange(value: Float): Boolean = value in min..max
    fun isOptimal(value: Float): Boolean = value in (optimal - 5)..( optimal + 5)
}

/**
 * Estados de conexión
 */
enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR
}

/**
 * Datos de salud basados en los sensores
 */
data class HealthStatus(
    val temperature: String = "Normal",
    val heartRate: String = "Normal",
    val movement: String = "Quiet",
    val ambientLight: String = "Normal",
    val overall: String = "Healthy"
)


// Rangos de valores normales para la salud
object SensorRanges {
    val TEMPERATURE = SensorRange(min = 18f, max = 25f, optimal = 20.5f)
    val HUMIDITY = SensorRange(min = 30f, max = 70f, optimal = 50f)
    val HEART_RATE = SensorRange(min = 60f, max = 100f, optimal = 72f)
    val ACCELERATION = SensorRange(min = 0f, max = 2f, optimal = 0.5f)
    val LIGHT = SensorRange(min = 0f, max = 100f, optimal = 30f)
}

