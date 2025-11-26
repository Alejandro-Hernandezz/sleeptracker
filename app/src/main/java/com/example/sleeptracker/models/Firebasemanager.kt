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

/**
 * Calidad del sueño basada en los datos de sensores
 */
data class SleepQuality(
    val score: Int, // 0-100
    val level: SleepLevel,
    val factors: SleepFactors,
    val recommendation: String
)

enum class SleepLevel {
    EXCELLENT,  // 90-100
    GOOD,       // 70-89
    FAIR,       // 50-69
    POOR        // 0-49
}

data class SleepFactors(
    val temperatureScore: Int,
    val humidityScore: Int,
    val heartRateScore: Int,
    val movementScore: Int,
    val lightScore: Int
)

/**
 * Analizador de calidad de sueño
 */
object SleepAnalyzer {

    fun analyze(
        temperatura: Float,
        humedad: Float,
        pulso: Int,
        aceleracion: Float,
        luz: Int
    ): SleepQuality {

        // Calcular puntuación individual de cada factor (0-20 puntos cada uno)
        val tempScore = calculateTemperatureScore(temperatura)
        val humScore = calculateHumidityScore(humedad)
        val heartScore = calculateHeartRateScore(pulso)
        val movementScore = calculateMovementScore(aceleracion)
        val lightScore = calculateLightScore(luz)

        // Puntuación total (0-100)
        val totalScore = tempScore + humScore + heartScore + movementScore + lightScore

        // Determinar nivel
        val level = when {
            totalScore >= 90 -> SleepLevel.EXCELLENT
            totalScore >= 70 -> SleepLevel.GOOD
            totalScore >= 50 -> SleepLevel.FAIR
            else -> SleepLevel.POOR
        }

        // Generar recomendación
        val recommendation = generateRecommendation(
            tempScore, humScore, heartScore, movementScore, lightScore
        )

        return SleepQuality(
            score = totalScore,
            level = level,
            factors = SleepFactors(tempScore, humScore, heartScore, movementScore, lightScore),
            recommendation = recommendation
        )
    }

    private fun calculateTemperatureScore(temp: Float): Int {
        return when {
            temp in 18f..22f -> 20 // Ideal
            temp in 16f..18f || temp in 22f..24f -> 15 // Bueno
            temp in 14f..16f || temp in 24f..26f -> 10 // Regular
            else -> 5 // Malo
        }
    }

    private fun calculateHumidityScore(hum: Float): Int {
        return when {
            hum in 40f..60f -> 20 // Ideal
            hum in 30f..40f || hum in 60f..70f -> 15 // Bueno
            hum in 20f..30f || hum in 70f..80f -> 10 // Regular
            else -> 5 // Malo
        }
    }

    private fun calculateHeartRateScore(bpm: Int): Int {
        return when {
            bpm in 60..75 -> 20 // Ideal (reposo)
            bpm in 55..60 || bpm in 75..85 -> 15 // Bueno
            bpm in 50..55 || bpm in 85..100 -> 10 // Regular
            else -> 5 // Malo
        }
    }

    private fun calculateMovementScore(acel: Float): Int {
        return when {
            acel < 0.3f -> 20 // Muy quieto - excelente
            acel < 0.7f -> 15 // Poco movimiento - bueno
            acel < 1.2f -> 10 // Movimiento moderado - regular
            else -> 5 // Mucho movimiento - malo
        }
    }

    private fun calculateLightScore(luz: Int): Int {
        return when {
            luz < 10 -> 20 // Muy oscuro - excelente
            luz < 30 -> 15 // Oscuro - bueno
            luz < 50 -> 10 // Algo de luz - regular
            else -> 5 // Mucha luz - malo
        }
    }

    private fun generateRecommendation(
        tempScore: Int, humScore: Int, heartScore: Int,
        movementScore: Int, lightScore: Int
    ): String {
        val issues = mutableListOf<String>()

        if (tempScore <= 10) issues.add("temperatura")
        if (humScore <= 10) issues.add("humedad")
        if (heartScore <= 10) issues.add("ritmo cardíaco")
        if (movementScore <= 10) issues.add("movimiento excesivo")
        if (lightScore <= 10) issues.add("luz ambiental")

        return when {
            issues.isEmpty() -> "Condiciones óptimas para dormir"
            issues.size == 1 -> "Ajusta ${issues[0]}"
            issues.size == 2 -> "Ajusta ${issues[0]} y ${issues[1]}"
            else -> "Mejora ${issues.take(2).joinToString(", ")} y otros factores"
        }
    }
}

