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
    
    // Selected date state
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    
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
        exerciseLogs,
        selectedDate
    ) { glucoseLogs, dietLogs, medicationLogs, exerciseLogs, selectedDate ->
        DashboardData(
            latestGlucose = getLatestGlucose(glucoseLogs, selectedDate),
            glucoseTrend = getGlucoseTrend(glucoseLogs, selectedDate),
            todayCalories = getTodayCalories(dietLogs, selectedDate),
            mealCalories = getMealCalories(dietLogs, selectedDate),
            exerciseMinutes = getTodayExerciseMinutes(exerciseLogs, selectedDate),
            medicationCount = getTodayMedicationCount(medicationLogs, selectedDate),
            glucoseReadings = getGlucoseReadings(glucoseLogs, selectedDate),
            weeklyTrend = getWeeklyTrend(glucoseLogs, selectedDate),
            selectedDate = selectedDate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardData())
    
    // Navigation
    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }
    
    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }
    
    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }
    
    fun canGoForward(): Boolean {
        return _selectedDate.value.isBefore(LocalDate.now())
    }
    
    private fun getLatestGlucose(logs: List<BloodGlucoseLogEntity>, selectedDate: LocalDate): Float? {
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
            .maxByOrNull { it.dateTime }?.value
    }
    
    private fun getGlucoseTrend(logs: List<BloodGlucoseLogEntity>, selectedDate: LocalDate): List<Float> {
        val userLogs = logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
        return userLogs
            .sortedBy { it.dateTime }
            .map { it.value }
    }
    
    private fun getTodayCalories(logs: List<DietLogEntity>, selectedDate: LocalDate): Int {
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
            .sumOf { it.calories ?: 0 }
    }
    
    private fun getMealCalories(logs: List<DietLogEntity>, selectedDate: LocalDate): MealCalories {
        val selectedDateLogs = logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
        
        return MealCalories(
            breakfast = selectedDateLogs
                .filter { it.period == EntryPeriod.BREAKFAST }
                .sumOf { it.calories ?: 0 },
            lunch = selectedDateLogs
                .filter { it.period == EntryPeriod.LUNCH }
                .sumOf { it.calories ?: 0 },
            dinner = selectedDateLogs
                .filter { it.period == EntryPeriod.DINNER }
                .sumOf { it.calories ?: 0 },
            snack = selectedDateLogs
                .filter { it.period == EntryPeriod.SNACK }
                .sumOf { it.calories ?: 0 }
        )
    }
    
    private fun getWaterIntake(logs: List<DietLogEntity>, selectedDate: LocalDate): Int {
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
            .filter { it.foodItems?.any { item -> 
                item.lowercase().contains("water") || 
                item.lowercase().contains("drink") ||
                item.lowercase().contains("liquid")
            } == true }
            .count() * 250 // Assume 250ml per entry
    }
    
    private fun getTodayExerciseMinutes(logs: List<ExerciseLogEntity>, selectedDate: LocalDate): Int {
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
            .sumOf { it.durationMinutes }
    }
    
    private fun getTodayMedicationCount(logs: List<MedicationLogEntity>, selectedDate: LocalDate): Int {
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
            .count()
    }
    
    private fun getGlucoseReadings(logs: List<BloodGlucoseLogEntity>, selectedDate: LocalDate): Int {
        return logs
            .filter { it.userId == currentUserId }
            .filter { it.dateTime.toLocalDate() == selectedDate }
            .count()
    }
    
    private fun getWeeklyTrend(logs: List<BloodGlucoseLogEntity>, selectedDate: LocalDate): List<DailyGlucose> {
        val endDate = selectedDate
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
    val weeklyTrend: List<DailyGlucose> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now()
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
