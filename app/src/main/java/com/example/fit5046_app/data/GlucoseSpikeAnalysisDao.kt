package com.example.fit5046_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for glucose spike analysis entities
 */
@Dao
interface GlucoseSpikeAnalysisDao {
    
    // Glucose Spike Analysis
    @Query("SELECT * FROM glucose_spike_analysis WHERE userId = :userId ORDER BY analysisDate DESC")
    fun getAllAnalysesForUser(userId: String): Flow<List<GlucoseSpikeAnalysisEntity>>
    
    @Query("SELECT * FROM glucose_spike_analysis WHERE userId = :userId ORDER BY analysisDate DESC LIMIT 1")
    fun getLatestAnalysisForUser(userId: String): Flow<GlucoseSpikeAnalysisEntity?>
    
    @Query("SELECT * FROM glucose_spike_analysis WHERE id = :analysisId")
    suspend fun getAnalysisById(analysisId: Int): GlucoseSpikeAnalysisEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: GlucoseSpikeAnalysisEntity): Long
    
    @Delete
    suspend fun deleteAnalysis(analysis: GlucoseSpikeAnalysisEntity)
    
    @Query("DELETE FROM glucose_spike_analysis WHERE userId = :userId")
    suspend fun deleteAllAnalysesForUser(userId: String)
    
    // Glucose Spikes
    @Query("SELECT * FROM glucose_spikes WHERE analysisId = :analysisId ORDER BY spikeTime DESC")
    fun getSpikesForAnalysis(analysisId: Int): Flow<List<GlucoseSpikeEntity>>
    
    @Query("SELECT * FROM glucose_spikes WHERE userId = :userId ORDER BY spikeTime DESC")
    fun getAllSpikesForUser(userId: String): Flow<List<GlucoseSpikeEntity>>
    
    @Query("SELECT * FROM glucose_spikes WHERE userId = :userId AND spikeTime BETWEEN :startDate AND :endDate ORDER BY spikeTime DESC")
    fun getSpikesInDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<GlucoseSpikeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpike(spike: GlucoseSpikeEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpikes(spikes: List<GlucoseSpikeEntity>)
    
    @Delete
    suspend fun deleteSpike(spike: GlucoseSpikeEntity)
    
    @Query("DELETE FROM glucose_spikes WHERE analysisId = :analysisId")
    suspend fun deleteSpikesForAnalysis(analysisId: Int)
    
    // Food-Glucose Correlations
    @Query("SELECT * FROM food_glucose_correlations WHERE analysisId = :analysisId ORDER BY correlationScore DESC")
    fun getCorrelationsForAnalysis(analysisId: Int): Flow<List<FoodGlucoseCorrelationEntity>>
    
    @Query("SELECT * FROM food_glucose_correlations WHERE userId = :userId ORDER BY correlationScore DESC")
    fun getAllCorrelationsForUser(userId: String): Flow<List<FoodGlucoseCorrelationEntity>>
    
    @Query("SELECT * FROM food_glucose_correlations WHERE userId = :userId AND foodItem = :foodItem ORDER BY analysisId DESC LIMIT 1")
    suspend fun getLatestCorrelationForFood(userId: String, foodItem: String): FoodGlucoseCorrelationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrelation(correlation: FoodGlucoseCorrelationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrelations(correlations: List<FoodGlucoseCorrelationEntity>)
    
    @Delete
    suspend fun deleteCorrelation(correlation: FoodGlucoseCorrelationEntity)
    
    @Query("DELETE FROM food_glucose_correlations WHERE analysisId = :analysisId")
    suspend fun deleteCorrelationsForAnalysis(analysisId: Int)
    
    // Food Recommendations
    @Query("SELECT * FROM food_recommendations WHERE analysisId = :analysisId ORDER BY confidence DESC")
    fun getRecommendationsForAnalysis(analysisId: Int): Flow<List<FoodRecommendationEntity>>
    
    @Query("SELECT * FROM food_recommendations WHERE userId = :userId ORDER BY confidence DESC")
    fun getAllRecommendationsForUser(userId: String): Flow<List<FoodRecommendationEntity>>
    
    @Query("SELECT * FROM food_recommendations WHERE userId = :userId AND recommendationType = :recommendationType ORDER BY confidence DESC")
    fun getRecommendationsByType(userId: String, recommendationType: String): Flow<List<FoodRecommendationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: FoodRecommendationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<FoodRecommendationEntity>)
    
    @Delete
    suspend fun deleteRecommendation(recommendation: FoodRecommendationEntity)
    
    @Query("DELETE FROM food_recommendations WHERE analysisId = :analysisId")
    suspend fun deleteRecommendationsForAnalysis(analysisId: Int)
    
    // Combined queries for analysis results
    @Transaction
    @Query("SELECT * FROM glucose_spike_analysis WHERE id = :analysisId")
    suspend fun getCompleteAnalysis(analysisId: Int): CompleteAnalysisResult?
    
    @Transaction
    @Query("SELECT * FROM glucose_spike_analysis WHERE userId = :userId ORDER BY analysisDate DESC LIMIT 1")
    suspend fun getLatestCompleteAnalysis(userId: String): CompleteAnalysisResult?
}

/**
 * Data class for complete analysis results with all related entities
 */
data class CompleteAnalysisResult(
    @Embedded val analysis: GlucoseSpikeAnalysisEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "analysisId"
    )
    val spikes: List<GlucoseSpikeEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "analysisId"
    )
    val correlations: List<FoodGlucoseCorrelationEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "analysisId"
    )
    val recommendations: List<FoodRecommendationEntity>
)
