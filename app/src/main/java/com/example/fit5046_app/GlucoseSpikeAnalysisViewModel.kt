package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.data.*
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.MealType
import com.example.fit5046_app.service.GlucoseSpikeAnalysisService
import com.example.fit5046_app.service.GlucoseSpikeAnalysisService.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class GlucoseSpikeAnalysisViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: LogRepository = LogRepository(application)
    private val analysisService: GlucoseSpikeAnalysisService = GlucoseSpikeAnalysisService()
    private val auth = FirebaseAuth.getInstance()
    
    private val currentUserId = auth.currentUser?.uid ?: "anonymous"
    
    // Data flows
    val latestAnalysis = repository.getLatestAnalysisForUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val allAnalyses = repository.getAllAnalysesForUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allSpikes = repository.getAllSpikesForUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allCorrelations = repository.getAllCorrelationsForUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allRecommendations = repository.getAllRecommendationsForUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Analysis state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()
    
    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()
    
    // Combined analysis data
    val analysisData = combine(
        latestAnalysis,
        allSpikes,
        allCorrelations,
        allRecommendations
    ) { analysis, spikes, correlations, recommendations ->
        AnalysisData(
            analysis = analysis,
            spikes = spikes,
            correlations = correlations,
            recommendations = recommendations
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalysisData())
    
    
    fun runAnalysis() {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                _analysisError.value = null
                _analysisProgress.value = 0f
                
                // Get current data
                val glucoseLogs = repository.getAllBloodGlucoseLogs().first()
                val dietLogs = repository.getAllDietLogs().first()
                
                _analysisProgress.value = 0.3f
                
                // Filter data for current user
                val userGlucoseLogs = glucoseLogs.filter { it.userId == currentUserId }
                val userDietLogs = dietLogs.filter { it.userId == currentUserId }
                
                _analysisProgress.value = 0.5f
                
                // Run analysis
                val analysisResult = analysisService.analyzeGlucoseSpikes(userGlucoseLogs, userDietLogs)
                
                _analysisProgress.value = 0.8f
                
                // Save results to database
                saveAnalysisResults(analysisResult)
                
                _analysisProgress.value = 1f
                
            } catch (e: Exception) {
                _analysisError.value = "Analysis failed: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    
    fun runAnalysisForDateRange(startDate: LocalDateTime, endDate: LocalDateTime) {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                _analysisError.value = null
                _analysisProgress.value = 0f
                
                // Get data for date range
                val glucoseLogs = repository.getAllBloodGlucoseLogs().first()
                    .filter { it.userId == currentUserId && it.dateTime.isAfter(startDate) && it.dateTime.isBefore(endDate) }
                
                val dietLogs = repository.getAllDietLogs().first()
                    .filter { it.userId == currentUserId && it.dateTime.isAfter(startDate) && it.dateTime.isBefore(endDate) }
                
                _analysisProgress.value = 0.5f
                
                // Run analysis
                val analysisResult = analysisService.analyzeGlucoseSpikes(glucoseLogs, dietLogs)
                
                _analysisProgress.value = 0.8f
                
                // Save results to database
                saveAnalysisResults(analysisResult)
                
                _analysisProgress.value = 1f
                
            } catch (e: Exception) {
                _analysisError.value = "Analysis failed: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    
    fun getRecommendationsByType(recommendationType: FoodRecommendationType): StateFlow<List<FoodRecommendationEntity>> {
        return repository.getRecommendationsByType(currentUserId, recommendationType.name)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    
    fun getSpikesInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): StateFlow<List<GlucoseSpikeEntity>> {
        return repository.getSpikesInDateRange(currentUserId, startDate, endDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    
    fun deleteAnalysis(analysis: GlucoseSpikeAnalysisEntity) {
        viewModelScope.launch {
            repository.deleteAnalysis(analysis)
        }
    }
    
    
    fun clearError() {
        _analysisError.value = null
    }
    
    
    /**
     * Save analysis results to database
     */
    private suspend fun saveAnalysisResults(result: GlucoseSpikeAnalysisResult) {
        val analysis = GlucoseSpikeAnalysisEntity(
            userId = currentUserId,
            analysisDate = LocalDateTime.now(),
            analysisPeriodStart = result.analysisPeriod.startDate,
            analysisPeriodEnd = result.analysisPeriod.endDate,
            daysAnalyzed = result.analysisPeriod.daysAnalyzed,
            totalGlucoseReadings = result.totalReadings,
            totalSpikesDetected = result.spikes.size,
            averageGlucose = result.averageGlucose,
            timeInRange = result.timeInRange
        )
        
        val spikes = result.spikes.map { spike ->
            GlucoseSpikeEntity(
                analysisId = 0, // Will be set by repository
                userId = currentUserId,
                spikeTime = spike.spikeTime,
                spikeValue = spike.spikeValue,
                baselineValue = spike.baselineValue,
                spikeMagnitude = spike.spikeMagnitude,
                spikeDuration = spike.spikeDuration,
                confidence = spike.confidence,
                possibleCauses = spike.possibleCauses
            )
        }
        
        val correlations = result.foodCorrelations.map { correlation ->
            FoodGlucoseCorrelationEntity(
                analysisId = 0, // Will be set by repository
                userId = currentUserId,
                foodItem = correlation.foodItem,
                correlationScore = correlation.correlationScore,
                spikeFrequency = correlation.spikeFrequency,
                averageSpikeMagnitude = correlation.averageSpikeMagnitude,
                sampleSize = correlation.sampleSize,
                lastConsumed = correlation.lastConsumed,
                recommendationType = correlation.recommendation.name
            )
        }
        
        val recommendations = result.recommendations.map { recommendation ->
            FoodRecommendationEntity(
                analysisId = 0, // Will be set by repository
                userId = currentUserId,
                foodItem = recommendation.foodItem,
                recommendationType = recommendation.recommendationType.name,
                reason = recommendation.reason,
                alternativeSuggestions = recommendation.alternativeSuggestions,
                confidence = recommendation.confidence
            )
        }
        
        repository.saveAnalysisResults(analysis, spikes, correlations, recommendations)
    }
}


data class AnalysisData(
    val analysis: GlucoseSpikeAnalysisEntity? = null,
    val spikes: List<GlucoseSpikeEntity> = emptyList(),
    val correlations: List<FoodGlucoseCorrelationEntity> = emptyList(),
    val recommendations: List<FoodRecommendationEntity> = emptyList()
)
