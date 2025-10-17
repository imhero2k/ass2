package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.DietLogEntity
import com.example.fit5046_app.data.ExerciseLogEntity
import com.example.fit5046_app.data.MedicationLogEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    // Get repository from application class (single shared instance)
    private val repository: LogRepository = 
        (application as GlucoseDiaryApplication).logRepository
    private val auth = FirebaseAuth.getInstance()
    
    private val currentUserId = auth.currentUser?.uid ?: "anonymous"
    
    // Data flows
    val bloodGlucoseLogs = repository.getAllBloodGlucoseLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val dietLogs = repository.getAllDietLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val exerciseLogs = repository.getAllExerciseLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val medicationLogs = repository.getAllMedicationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Reports data
    val reportsData = combine(
        bloodGlucoseLogs,
        dietLogs,
        exerciseLogs,
        medicationLogs
    ) { glucoseLogs, dietLogs, exerciseLogs, medicationLogs ->
        ReportsData(
            glucoseAnalysis = getGlucoseAnalysis(glucoseLogs),
            dietAnalysis = getDietAnalysis(dietLogs),
            exerciseAnalysis = getExerciseAnalysis(exerciseLogs)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsData())
    
    private fun getGlucoseAnalysis(logs: List<BloodGlucoseLogEntity>): GlucoseAnalysis {
        val userLogs = logs.filter { it.userId == currentUserId }
        val last7Days = userLogs.filter { 
            it.dateTime.toLocalDate().isAfter(LocalDate.now().minusDays(7)) 
        }
        
        if (last7Days.isEmpty()) {
            return GlucoseAnalysis(
                averageGlucose = 0f,
                goodReadings = 0,
                highReadings = 0,
                lowReadings = 0,
                totalReadings = 0,
                dailyAverages = emptyList()
            )
        }
        
        val averageGlucose = last7Days.map { it.value }.average().toFloat()
        val goodReadings = last7Days.count { it.value in 4f..7f }
        val highReadings = last7Days.count { it.value > 7f }
        val lowReadings = last7Days.count { it.value < 4f }
        val totalReadings = last7Days.size
        
        // Calculate daily averages for the last 7 days
        val dailyAverages = (0..6).map { daysBack ->
            val date = LocalDate.now().minusDays(daysBack.toLong())
            val dayLogs = last7Days.filter { it.dateTime.toLocalDate() == date }
            val dayAverage = if (dayLogs.isNotEmpty()) {
                dayLogs.map { it.value }.average().toFloat()
            } else null
            
            DailyGlucoseAverage(
                date = date,
                average = dayAverage
            )
        }.reversed()
        
        return GlucoseAnalysis(
            averageGlucose = averageGlucose,
            goodReadings = goodReadings,
            highReadings = highReadings,
            lowReadings = lowReadings,
            totalReadings = totalReadings,
            dailyAverages = dailyAverages
        )
    }
    
    private fun getDietAnalysis(logs: List<DietLogEntity>): DietAnalysis {
        val userLogs = logs.filter { it.userId == currentUserId }
        val last7Days = userLogs.filter { 
            it.dateTime.toLocalDate().isAfter(LocalDate.now().minusDays(7)) 
        }
        
        if (last7Days.isEmpty()) {
            return DietAnalysis(
                averageDailyCalories = 0,
                averageCarbs = 0f,
                averageProtein = 0f,
                averageFats = 0f,
                dailyCalories = emptyList()
            )
        }
        
        val totalCalories = last7Days.sumOf { it.calories ?: 0 }
        val totalCarbs = last7Days.sumOf { it.carbGrams?.toDouble() ?: 0.0 }.toFloat()
        val totalProtein = last7Days.sumOf { it.proteinGrams?.toDouble() ?: 0.0 }.toFloat()
        val totalFats = last7Days.sumOf { it.fatGrams?.toDouble() ?: 0.0 }.toFloat()
        
        val daysWithData = last7Days.map { it.dateTime.toLocalDate() }.distinct().size
        val averageDailyCalories = if (daysWithData > 0) totalCalories / daysWithData else 0
        val averageCarbs = if (daysWithData > 0) totalCarbs / daysWithData else 0f
        val averageProtein = if (daysWithData > 0) totalProtein / daysWithData else 0f
        val averageFats = if (daysWithData > 0) totalFats / daysWithData else 0f
        
        // Calculate daily calories for the last 7 days
        val dailyCalories = (0..6).map { daysBack ->
            val date = LocalDate.now().minusDays(daysBack.toLong())
            val dayLogs = last7Days.filter { it.dateTime.toLocalDate() == date }
            val dayCalories = dayLogs.sumOf { it.calories ?: 0 }
            
            DailyCalories(
                date = date,
                calories = dayCalories
            )
        }.reversed()
        
        return DietAnalysis(
            averageDailyCalories = averageDailyCalories,
            averageCarbs = averageCarbs,
            averageProtein = averageProtein,
            averageFats = averageFats,
            dailyCalories = dailyCalories
        )
    }
    
    private fun getExerciseAnalysis(logs: List<ExerciseLogEntity>): ExerciseAnalysis {
        val userLogs = logs.filter { it.userId == currentUserId }
        val last7Days = userLogs.filter { 
            it.dateTime.toLocalDate().isAfter(LocalDate.now().minusDays(7)) 
        }
        
        if (last7Days.isEmpty()) {
            return ExerciseAnalysis(
                totalMinutes = 0,
                averageMinutesPerDay = 0f,
                exerciseTypes = emptyMap(),
                dailyMinutes = emptyList()
            )
        }
        
        val totalMinutes = last7Days.sumOf { it.durationMinutes }
        val daysWithExercise = last7Days.map { it.dateTime.toLocalDate() }.distinct().size
        val averageMinutesPerDay = if (daysWithExercise > 0) totalMinutes.toFloat() / daysWithExercise else 0f
        
        // Count exercise types
        val exerciseTypes = last7Days.groupBy { it.exerciseType }
            .mapValues { it.value.sumOf { exercise -> exercise.durationMinutes } }
        
        // Calculate daily exercise minutes for the last 7 days
        val dailyMinutes = (0..6).map { daysBack ->
            val date = LocalDate.now().minusDays(daysBack.toLong())
            val dayLogs = last7Days.filter { it.dateTime.toLocalDate() == date }
            val dayMinutes = dayLogs.sumOf { it.durationMinutes }
            
            DailyExercise(
                date = date,
                minutes = dayMinutes
            )
        }.reversed()
        
        return ExerciseAnalysis(
            totalMinutes = totalMinutes,
            averageMinutesPerDay = averageMinutesPerDay,
            exerciseTypes = exerciseTypes,
            dailyMinutes = dailyMinutes
        )
    }
}

data class ReportsData(
    val glucoseAnalysis: GlucoseAnalysis = GlucoseAnalysis(),
    val dietAnalysis: DietAnalysis = DietAnalysis(),
    val exerciseAnalysis: ExerciseAnalysis = ExerciseAnalysis()
)

data class GlucoseAnalysis(
    val averageGlucose: Float = 0f,
    val goodReadings: Int = 0,
    val highReadings: Int = 0,
    val lowReadings: Int = 0,
    val totalReadings: Int = 0,
    val dailyAverages: List<DailyGlucoseAverage> = emptyList()
)

data class DailyGlucoseAverage(
    val date: LocalDate,
    val average: Float?
)

data class DietAnalysis(
    val averageDailyCalories: Int = 0,
    val averageCarbs: Float = 0f,
    val averageProtein: Float = 0f,
    val averageFats: Float = 0f,
    val dailyCalories: List<DailyCalories> = emptyList()
)

data class DailyCalories(
    val date: LocalDate,
    val calories: Int
)

data class ExerciseAnalysis(
    val totalMinutes: Int = 0,
    val averageMinutesPerDay: Float = 0f,
    val exerciseTypes: Map<com.example.fit5046_app.model.ExerciseType, Int> = emptyMap(),
    val dailyMinutes: List<DailyExercise> = emptyList()
)

data class DailyExercise(
    val date: LocalDate,
    val minutes: Int
)
