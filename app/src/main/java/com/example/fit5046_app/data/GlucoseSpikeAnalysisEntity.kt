package com.example.fit5046_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity for storing glucose spike analysis results
 */
@Entity(
    tableName = "glucose_spike_analysis"
)
data class GlucoseSpikeAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String, // Firebase Auth UID
    val analysisDate: LocalDateTime,
    val analysisPeriodStart: LocalDateTime,
    val analysisPeriodEnd: LocalDateTime,
    val daysAnalyzed: Int,
    val totalGlucoseReadings: Int,
    val totalSpikesDetected: Int,
    val averageGlucose: Float,
    val timeInRange: Float, // Percentage of time glucose was in target range
    val analysisVersion: String = "1.0" // For future compatibility
)

/**
 * Entity for storing individual glucose spikes
 */
@Entity(
    tableName = "glucose_spikes"
)
data class GlucoseSpikeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val analysisId: Int, // Foreign key to GlucoseSpikeAnalysisEntity
    val userId: String,
    val spikeTime: LocalDateTime,
    val spikeValue: Float,
    val baselineValue: Float,
    val spikeMagnitude: Float,
    val spikeDuration: Long, // Duration in minutes
    val confidence: Float,
    val possibleCauses: List<String>? = null
)

/**
 * Entity for storing food-glucose correlations
 */
@Entity(
    tableName = "food_glucose_correlations"
)
data class FoodGlucoseCorrelationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val analysisId: Int, // Foreign key to GlucoseSpikeAnalysisEntity
    val userId: String,
    val foodItem: String,
    val correlationScore: Float, // -1 to 1
    val spikeFrequency: Float, // 0 to 1
    val averageSpikeMagnitude: Float,
    val sampleSize: Int,
    val lastConsumed: LocalDateTime?,
    val recommendationType: String // "AVOID", "MODERATE", "SAFE", "BENEFICIAL"
)

/**
 * Entity for storing food recommendations
 */
@Entity(
    tableName = "food_recommendations"
)
data class FoodRecommendationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val analysisId: Int, // Foreign key to GlucoseSpikeAnalysisEntity
    val userId: String,
    val foodItem: String,
    val recommendationType: String,
    val reason: String,
    val alternativeSuggestions: List<String>?,
    val confidence: Float
)
