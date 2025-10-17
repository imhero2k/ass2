package com.example.fit5046_app.service

import android.util.Log
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.DietLogEntity
import com.example.fit5046_app.model.EntryPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs


class GlucoseSpikeAnalysisService {
    
    companion object {
        private const val TAG = "GlucoseSpikeAnalysis"
        
        // Normal glucose range (mmol/L)
        private const val GLUCOSE_LOWER_NORMAL = 4.0f // 4.0 mmol/L
        private const val GLUCOSE_UPPER_NORMAL = 7.8f // 7.8 mmol/L
        private const val GLUCOSE_SPIKE_THRESHOLD = 7.8f // Only show spikes > 7.8 mmol/L
        
        // Spike detection parameters
        private const val BASELINE_WINDOW_HOURS = 2L // Look back 2 hours for baseline
        private const val FOOD_CORRELATION_WINDOW_HOURS = 4L // Look back 4 hours for food correlation
        private const val MIN_GLUCOSE_READINGS_FOR_ANALYSIS = 3 // Minimum readings needed for analysis
    }
    
    
    data class GlucoseSpikeAnalysisResult(
        val spikes: List<GlucoseSpike>,
        val foodCorrelations: List<FoodGlucoseCorrelation>,
        val recommendations: List<FoodRecommendation>,
        val analysisPeriod: AnalysisPeriod,
        val totalReadings: Int,
        val averageGlucose: Float,
        val timeInRange: Float // Percentage of time glucose was in target range (4.0-7.8 mmol/L)
    )
    
   
    data class GlucoseSpike(
        val spikeTime: LocalDateTime,
        val spikeValue: Float,
        val baselineValue: Float,
        val spikeMagnitude: Float, // How much above baseline
        val spikeDuration: Long, // Duration in minutes
        val possibleCauses: List<String>, // Food items that might have caused the spike
        val confidence: Float // Confidence in spike detection (0-1)
    )
    
    
    data class FoodGlucoseCorrelation(
        val foodItem: String,
        val correlationScore: Float, // -1 to 1, where 1 is strong positive correlation
        val spikeFrequency: Float, // How often this food is followed by spikes (0-1)
        val averageSpikeMagnitude: Float, // Average spike size when this food is consumed
        val sampleSize: Int, // Number of times this food was consumed
        val lastConsumed: LocalDateTime?,
        val recommendation: FoodRecommendationType
    )
    
    data class FoodRecommendation(
        val foodItem: String,
        val recommendationType: FoodRecommendationType,
        val reason: String,
        val alternativeSuggestions: List<String> = emptyList(),
        val confidence: Float
    )
    
    enum class FoodRecommendationType {
        AVOID, 
        MODERATE, 
        SAFE, 
        BENEFICIAL 
    }
    
    data class AnalysisPeriod(
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val daysAnalyzed: Int
    )
    
    suspend fun analyzeGlucoseSpikes(
        glucoseLogs: List<BloodGlucoseLogEntity>,
        dietLogs: List<DietLogEntity>
    ): GlucoseSpikeAnalysisResult = withContext(Dispatchers.Default) {
        
        Log.d(TAG, "Starting glucose spike analysis with ${glucoseLogs.size} glucose readings and ${dietLogs.size} diet entries")
        
        
        glucoseLogs.take(5).forEach { log ->
            Log.d(TAG, "Sample glucose: ${log.value} mmol/L at ${log.dateTime}")
        }
        
        if (glucoseLogs.size < MIN_GLUCOSE_READINGS_FOR_ANALYSIS) {
            Log.w(TAG, "Insufficient glucose readings for analysis")
            return@withContext createEmptyResult(glucoseLogs, dietLogs)
        }
        
        // Sort
        val sortedGlucoseLogs = glucoseLogs.sortedBy { it.dateTime }
        val sortedDietLogs = dietLogs.sortedBy { it.dateTime }
        
        // Detect
        val spikes = detectGlucoseSpikes(sortedGlucoseLogs)
        Log.d(TAG, "Detected ${spikes.size} glucose spikes")
        
        // Log 
        spikes.forEach { spike ->
            Log.d(TAG, "Spike: ${spike.spikeValue} mmol/L at ${spike.spikeTime}, magnitude: ${spike.spikeMagnitude}")
        }
        
        
        val foodCorrelations = correlateFoodWithSpikes(spikes, sortedDietLogs)
        Log.d(TAG, "Analyzed correlations for ${foodCorrelations.size} food items")
        
        
        val recommendations = generateFoodRecommendations(foodCorrelations)
        Log.d(TAG, "Generated ${recommendations.size} food recommendations")
        
        
        val analysisPeriod = calculateAnalysisPeriod(sortedGlucoseLogs)
        val averageGlucose = calculateAverageGlucose(sortedGlucoseLogs)
        val timeInRange = calculateTimeInRange(sortedGlucoseLogs)
        
        GlucoseSpikeAnalysisResult(
            spikes = spikes,
            foodCorrelations = foodCorrelations,
            recommendations = recommendations,
            analysisPeriod = analysisPeriod,
            totalReadings = glucoseLogs.size,
            averageGlucose = averageGlucose,
            timeInRange = timeInRange
        )
    }
    
    
    private fun detectGlucoseSpikes(glucoseLogs: List<BloodGlucoseLogEntity>): List<GlucoseSpike> {
        val spikes = mutableListOf<GlucoseSpike>()
        
        Log.d(TAG, "Starting spike detection with ${glucoseLogs.size} glucose readings")
        Log.d(TAG, "Glucose range: $GLUCOSE_LOWER_NORMAL - $GLUCOSE_UPPER_NORMAL mmol/L")
        
        for (i in 1 until glucoseLogs.size) {
            val currentReading = glucoseLogs[i]
            
            val isAboveThreshold = currentReading.value > GLUCOSE_SPIKE_THRESHOLD
            
            if (isAboveThreshold) {
                Log.d(TAG, "Found spike above threshold: ${currentReading.value} mmol/L at ${currentReading.dateTime}")
                
                val baselineReadings = glucoseLogs.filter { reading ->
                    val timeDiff = ChronoUnit.HOURS.between(reading.dateTime, currentReading.dateTime)
                    timeDiff in 1..BASELINE_WINDOW_HOURS && reading.dateTime < currentReading.dateTime
                }
                
                val baselineValue = if (baselineReadings.isNotEmpty()) {
                    baselineReadings.map { it.value }.average().toFloat()
                } else {
                    GLUCOSE_UPPER_NORMAL // Use upper normal as baseline if no baseline found
                }
                
                val spikeMagnitude = currentReading.value - baselineValue
                val possibleCauses = findPossibleFoodCauses(currentReading.dateTime, glucoseLogs)
                val confidence = calculateSpikeConfidence(spikeMagnitude, baselineReadings.size)
                
                val spike = GlucoseSpike(
                    spikeTime = currentReading.dateTime,
                    spikeValue = currentReading.value,
                    baselineValue = baselineValue,
                    spikeMagnitude = spikeMagnitude,
                    spikeDuration = calculateSpikeDuration(currentReading, glucoseLogs),
                    possibleCauses = possibleCauses,
                    confidence = confidence
                )
                
                spikes.add(spike)
                Log.d(TAG, "Added spike: ${spike.spikeValue} mmol/L, magnitude: ${spike.spikeMagnitude}")
            } else {
                val baselineReadings = glucoseLogs.filter { reading ->
                    val timeDiff = ChronoUnit.HOURS.between(reading.dateTime, currentReading.dateTime)
                    timeDiff in 1..BASELINE_WINDOW_HOURS && reading.dateTime < currentReading.dateTime
                }
                
                if (baselineReadings.isNotEmpty()) {
                    val baselineValue = baselineReadings.map { it.value }.average().toFloat()
                    val spikeMagnitude = currentReading.value - baselineValue
                    val isSignificantIncrease = spikeMagnitude > (GLUCOSE_UPPER_NORMAL - GLUCOSE_LOWER_NORMAL) * 0.5f // 50% of normal range
                    
                    if (isSignificantIncrease) {
                        Log.d(TAG, "Found significant increase: ${currentReading.value} mmol/L (baseline: $baselineValue)")
                        
                        val possibleCauses = findPossibleFoodCauses(currentReading.dateTime, glucoseLogs)
                        val confidence = calculateSpikeConfidence(spikeMagnitude, baselineReadings.size)
                        
                        val spike = GlucoseSpike(
                            spikeTime = currentReading.dateTime,
                            spikeValue = currentReading.value,
                            baselineValue = baselineValue,
                            spikeMagnitude = spikeMagnitude,
                            spikeDuration = calculateSpikeDuration(currentReading, glucoseLogs),
                            possibleCauses = possibleCauses,
                            confidence = confidence
                        )
                        
                        spikes.add(spike)
                    }
                }
            }
        }
        
        Log.d(TAG, "Detected ${spikes.size} total spikes")
        return spikes
    }
    
    
    private fun correlateFoodWithSpikes(
        spikes: List<GlucoseSpike>,
        dietLogs: List<DietLogEntity>
    ): List<FoodGlucoseCorrelation> {
        
        val foodSpikeMap = mutableMapOf<String, MutableList<Float>>()
        val foodConsumptionMap = mutableMapOf<String, MutableList<LocalDateTime>>()
        
        dietLogs.forEach { dietLog ->
            dietLog.foodItems?.forEach { foodItem ->
                foodConsumptionMap.getOrPut(foodItem) { mutableListOf() }.add(dietLog.dateTime)
            }
        }
        
        spikes.forEach { spike ->
            val foodsBeforeSpike = dietLogs.filter { dietLog ->
                val timeDiff = ChronoUnit.HOURS.between(dietLog.dateTime, spike.spikeTime)
                timeDiff in 0..FOOD_CORRELATION_WINDOW_HOURS && dietLog.dateTime <= spike.spikeTime
            }
            
            foodsBeforeSpike.forEach { dietLog ->
                dietLog.foodItems?.forEach { foodItem ->
                    foodSpikeMap.getOrPut(foodItem) { mutableListOf() }.add(spike.spikeMagnitude)
                }
            }
        }
        
        return foodConsumptionMap.map { (foodItem, consumptionTimes) ->
            val spikeMagnitudes = foodSpikeMap[foodItem] ?: emptyList()
            val spikeFrequency = if (consumptionTimes.isNotEmpty()) {
                spikeMagnitudes.size.toFloat() / consumptionTimes.size
            } else 0f
            
            val averageSpikeMagnitude = if (spikeMagnitudes.isNotEmpty()) {
                spikeMagnitudes.average().toFloat()
            } else 0f
            
            val correlationScore = calculateCorrelationScore(spikeFrequency, averageSpikeMagnitude)
            val recommendation = determineRecommendationType(correlationScore, spikeFrequency)
            
            FoodGlucoseCorrelation(
                foodItem = foodItem,
                correlationScore = correlationScore,
                spikeFrequency = spikeFrequency,
                averageSpikeMagnitude = averageSpikeMagnitude,
                sampleSize = consumptionTimes.size,
                lastConsumed = consumptionTimes.maxOrNull(),
                recommendation = recommendation
            )
        }.sortedByDescending { it.correlationScore }
    }
    
    
    private fun generateFoodRecommendations(
        correlations: List<FoodGlucoseCorrelation>
    ): List<FoodRecommendation> {
        
        // Group correlations by food item and take the one with highest correlation score
        val uniqueCorrelations = correlations
            .groupBy { it.foodItem }
            .mapValues { (_, correlationsForFood) ->
                correlationsForFood.maxByOrNull { abs(it.correlationScore) } ?: correlationsForFood.first()
            }
            .values
            .sortedByDescending { abs(it.correlationScore) }
        
        return uniqueCorrelations.map { correlation ->
            val baselineGlucose = 5.5f
            val peakGlucose = baselineGlucose + (correlation.averageSpikeMagnitude)
            val percentageIncrease = if (baselineGlucose > 0) {
                ((peakGlucose - baselineGlucose) / baselineGlucose) * 100f
            } else 0f
            
            val cappedPercentage = percentageIncrease.coerceAtMost(100f)
            
            val (reason, alternatives) = when (correlation.recommendation) {
                FoodRecommendationType.AVOID -> {
                    "Strong correlation with glucose spikes (${(correlation.spikeFrequency * 100).toInt()}% spike rate, +${cappedPercentage.toInt()}% glucose increase)" to
                    listOf("Consider low-carb alternatives", "Reduce portion size", "Consume with protein")
                }
                FoodRecommendationType.MODERATE -> {
                    "Moderate correlation with glucose spikes (${(correlation.spikeFrequency * 100).toInt()}% spike rate, +${cappedPercentage.toInt()}% glucose increase)" to
                    listOf("Monitor portion size", "Pair with fiber-rich foods", "Test blood glucose after consumption")
                }
                FoodRecommendationType.SAFE -> {
                    "No significant correlation with glucose spikes" to
                    listOf("Continue current consumption pattern", "Monitor for any changes")
                }
                FoodRecommendationType.BENEFICIAL -> {
                    "May help stabilize glucose levels" to
                    listOf("Consider increasing consumption", "Good choice for glucose management")
                }
            }
            
            FoodRecommendation(
                foodItem = correlation.foodItem,
                recommendationType = correlation.recommendation,
                reason = reason,
                alternativeSuggestions = alternatives,
                confidence = abs(correlation.correlationScore)
            )
        }
    }
    
    // Helper 
    private fun findPossibleFoodCauses(
        spikeTime: LocalDateTime,
        glucoseLogs: List<BloodGlucoseLogEntity>
    ): List<String> {
      
        return emptyList()
    }
    
    private fun calculateSpikeConfidence(spikeMagnitude: Float, baselineSampleSize: Int): Float {
        val normalRange = GLUCOSE_UPPER_NORMAL - GLUCOSE_LOWER_NORMAL
        val magnitudeFactor = minOf(spikeMagnitude / normalRange, 2.0f)
        val sampleFactor = minOf(baselineSampleSize / 3.0f, 1.0f)
        return (magnitudeFactor * sampleFactor).coerceIn(0f, 1f)
    }
    
    private fun calculateSpikeDuration(
        spikeReading: BloodGlucoseLogEntity,
        glucoseLogs: List<BloodGlucoseLogEntity>
    ): Long {
        val subsequentReadings = glucoseLogs.filter { it.dateTime > spikeReading.dateTime }
        return 60L
    }
    
    private fun calculateCorrelationScore(spikeFrequency: Float, averageSpikeMagnitude: Float): Float {
       val frequencyScore = spikeFrequency * 2 - 1 // Convert 0-1 to -1 to 1
        
        val baselineGlucose = 5.5f // Normal fasting glucose baseline
        val peakGlucose = baselineGlucose + averageSpikeMagnitude
        
        val percentageIncrease = if (baselineGlucose > 0) {
            ((peakGlucose - baselineGlucose) / baselineGlucose) * 100f
        } else 0f
        
        val cappedPercentage = percentageIncrease.coerceAtMost(100f)
        
        val magnitudeScore = (cappedPercentage / 100f).coerceIn(-1f, 1f)
        
        return (frequencyScore + magnitudeScore) / 2f
    }
    
    private fun determineRecommendationType(
        correlationScore: Float,
        spikeFrequency: Float
    ): FoodRecommendationType {
        return when {
            correlationScore > 0.6f && spikeFrequency > 0.7f -> FoodRecommendationType.AVOID
            correlationScore > 0.3f && spikeFrequency > 0.4f -> FoodRecommendationType.MODERATE
            correlationScore < -0.3f -> FoodRecommendationType.BENEFICIAL
            else -> FoodRecommendationType.SAFE
        }
    }
    
    private fun calculateAnalysisPeriod(glucoseLogs: List<BloodGlucoseLogEntity>): AnalysisPeriod {
        val startDate = glucoseLogs.minByOrNull { it.dateTime }?.dateTime ?: LocalDateTime.now()
        val endDate = glucoseLogs.maxByOrNull { it.dateTime }?.dateTime ?: LocalDateTime.now()
        val daysAnalyzed = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        
        return AnalysisPeriod(startDate, endDate, daysAnalyzed)
    }
    
    private fun calculateAverageGlucose(glucoseLogs: List<BloodGlucoseLogEntity>): Float {
        return if (glucoseLogs.isNotEmpty()) {
            glucoseLogs.map { it.value }.average().toFloat()
        } else 0f
    }
    
    private fun calculateTimeInRange(glucoseLogs: List<BloodGlucoseLogEntity>): Float {
        val inRangeReadings = glucoseLogs.count { it.value in GLUCOSE_LOWER_NORMAL..GLUCOSE_UPPER_NORMAL }
        return if (glucoseLogs.isNotEmpty()) {
            inRangeReadings.toFloat() / glucoseLogs.size * 100f
        } else 0f
    }
    
    private fun createEmptyResult(
        glucoseLogs: List<BloodGlucoseLogEntity>,
        dietLogs: List<DietLogEntity>
    ): GlucoseSpikeAnalysisResult {
        val analysisPeriod = calculateAnalysisPeriod(glucoseLogs)
        return GlucoseSpikeAnalysisResult(
            spikes = emptyList(),
            foodCorrelations = emptyList(),
            recommendations = emptyList(),
            analysisPeriod = analysisPeriod,
            totalReadings = glucoseLogs.size,
            averageGlucose = calculateAverageGlucose(glucoseLogs),
            timeInRange = calculateTimeInRange(glucoseLogs)
        )
    }
}
