package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.DietLogEntity
import com.example.fit5046_app.data.ExerciseLogEntity
import com.example.fit5046_app.data.MedicationLogEntity
import com.example.fit5046_app.model.EntryPeriod
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository = LogRepository(application)
    private val auth = FirebaseAuth.getInstance()
    
    private val currentUserId = auth.currentUser?.uid ?: "anonymous"
    
    // Data flows
    val bloodGlucoseLogs = repository.getAllBloodGlucoseLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val dietLogs = repository.getAllDietLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val medicationLogs = repository.getAllMedicationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val exerciseLogs = repository.getAllExerciseLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Dashboard data
    val dashboardData = combine(
        bloodGlucoseLogs,
        dietLogs,
        medicationLogs,
        exerciseLogs
    ) { glucoseLogs, dietLogs, medicationLogs, exerciseLogs ->
        DashboardData(
            latestGlucose = getLatestGlucose(glucoseLogs),
            glucoseTrend = getGlucoseTrend(glucoseLogs),
            todayCalories = getTodayCalories(dietLogs),
            mealCalories = getMealCalories(dietLogs),
            exerciseMinutes = getTodayExerciseMinutes(exerciseLogs),
            medicationCount = getTodayMedicationCount(medicationLogs),
            glucoseReadings = getGlucoseReadings(glucoseLogs),
            weeklyTrend = getWeeklyTrend(glucoseLogs)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardData())
    
    private fun getLatestGlucose(logs: List<BloodGlucoseLogEntity>): Float? {
        return logs
            .filter { it.userId == currentUserId }
            .maxByOrNull { it.dateTime }?.value
    }
    
    private fun getGlucoseTrend(logs: List<BloodGlucoseLogEntity>): List<Float> {
        val userLogs = logs.filter { it.userId == currentUserId }
        return userLogs
            .sortedBy { it.dateTime }
            .takeLast(7) // Last 7 readings
            .map { it.value }
    }
    
    private fun getTodayCalories(logs: List<DietLogEntity>): Int {
        val today = LocalDate.now()
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == today }
            .sumOf { it.calories ?: 0 }
    }
    
    private fun getMealCalories(logs: List<DietLogEntity>): MealCalories {
        val today = LocalDate.now()
        val todayLogs = logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == today }
        
        return MealCalories(
            breakfast = todayLogs
                .filter { it.period == EntryPeriod.BREAKFAST }
                .sumOf { it.calories ?: 0 },
            lunch = todayLogs
                .filter { it.period == EntryPeriod.LUNCH }
                .sumOf { it.calories ?: 0 },
            dinner = todayLogs
                .filter { it.period == EntryPeriod.DINNER }
                .sumOf { it.calories ?: 0 },
            snack = todayLogs
                .filter { it.period == EntryPeriod.SNACK }
                .sumOf { it.calories ?: 0 }
        )
    }
    
    private fun getWaterIntake(logs: List<DietLogEntity>): Int {
        val today = LocalDate.now()
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == today }
            .filter { it.foodItems?.any { item -> 
                item.lowercase().contains("water") || 
                item.lowercase().contains("drink") ||
                item.lowercase().contains("liquid")
            } == true }
            .count() * 250 // Assume 250ml per entry
    }
    
    private fun getTodayExerciseMinutes(logs: List<ExerciseLogEntity>): Int {
        val today = LocalDate.now()
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == today }
            .sumOf { it.durationMinutes }
    }
    
    private fun getTodayMedicationCount(logs: List<MedicationLogEntity>): Int {
        val today = LocalDate.now()
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == today }
            .count()
    }
    
    private fun getGlucoseReadings(logs: List<BloodGlucoseLogEntity>): Int {
        val today = LocalDate.now()
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == today }
            .count()
    }
    
    private fun getWeeklyTrend(logs: List<BloodGlucoseLogEntity>): List<DailyGlucose> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)
        
        return (0..6).map { daysBack ->
            val date = endDate.minusDays(daysBack.toLong())
            val dayLogs = logs
                .filter { it.userId == currentUserId }
                .filter { it.dateTime.toLocalDate() == date }
            
            val averageGlucose = if (dayLogs.isNotEmpty()) {
                dayLogs.map { it.value }.average().toFloat()
            } else null
            
            DailyGlucose(
                date = date,
                averageGlucose = averageGlucose,
                readingCount = dayLogs.size
            )
        }.reversed()
    }
}

data class DashboardData(
    val latestGlucose: Float? = null,
    val glucoseTrend: List<Float> = emptyList(),
    val todayCalories: Int = 0,
    val mealCalories: MealCalories = MealCalories(),
    val exerciseMinutes: Int = 0,
    val medicationCount: Int = 0,
    val glucoseReadings: Int = 0,
    val weeklyTrend: List<DailyGlucose> = emptyList()
)

data class MealCalories(
    val breakfast: Int = 0,
    val lunch: Int = 0,
    val dinner: Int = 0,
    val snack: Int = 0
)

data class DailyGlucose(
    val date: LocalDate,
    val averageGlucose: Float?,
    val readingCount: Int
)
