package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.service.OllamaService
import com.example.fit5046_app.service.UserContext
import com.example.fit5046_app.service.UserProfile
import com.example.fit5046_app.service.MealCalories
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.ZoneId

data class ContextAwareChatBotUiState(
    val messages: List<ChatMessage> = listOf(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false
)

class ContextAwareChatBotViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ContextAwareChatBotUiState())
    val uiState: StateFlow<ContextAwareChatBotUiState> = _uiState.asStateFlow()

    private val repository: LogRepository = LogRepository(application)
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    init {
        android.util.Log.d("ContextAwareChatBot", "ContextAwareChatBotViewModel initialized")
        android.util.Log.d("ContextAwareChatBot", "Current user ID: $currentUserId")

        // Add welcome message
        addMessage(
            ChatMessage(
                text = "Hello! I'm Diabetica, your AI assistant for diabetes management. I can help you with medication reminders, blood sugar monitoring, diet advice, exercise recommendations, and general diabetes care. How can I assist you today?",
                isUser = false
            )
        )

        // Test connection
        testConnection()
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        android.util.Log.d("ContextAwareChatBot", "sendMessage() called")
        val currentInput = _uiState.value.inputText.trim()
        android.util.Log.d("ContextAwareChatBot", "Current input: '$currentInput'")
        if (currentInput.isBlank() || _uiState.value.isLoading) {
            android.util.Log.d("ContextAwareChatBot", "Message is blank or loading, returning")
            return
        }

        // Add user message
        android.util.Log.d("ContextAwareChatBot", "Adding user message")
        addMessage(ChatMessage(text = currentInput, isUser = true))

        // Clear input and set loading
        android.util.Log.d("ContextAwareChatBot", "Setting loading state")
        _uiState.value = _uiState.value.copy(
            inputText = "",
            isLoading = true
        )

        // Get user context and AI response
        viewModelScope.launch {
            try {
                android.util.Log.d("ContextAwareChatBot", "About to call getUserContext()")

                // Create a simple test context first
                val testContext = UserContext(
                    latestGlucose = 6.8f,
                    glucoseTrend = listOf(6.5f, 7.2f, 6.8f),
                    todayCalories = 1850,
                    exerciseMinutes = 30,
                    medicationCount = 2,
                    glucoseReadings = 3,
                    mealCalories = MealCalories(breakfast = 450, lunch = 600, dinner = 800),
                    userProfile = UserProfile(
                        name = "Test User",
                        age = 45,
                        diabetesType = "Type 2",
                        targetGlucose = 7.0f,
                        medications = listOf("Metformin", "Insulin")
                    )
                )
                android.util.Log.d("ContextAwareChatBot", "Using test context: $testContext")

                val userContext = getUserContext()
                android.util.Log.d("ContextAwareChatBot", "User context returned: $userContext")
                android.util.Log.d("ContextAwareChatBot", "About to call OllamaService.generateResponse()")
                val response = OllamaService.generateResponse(currentInput, testContext) // Use test context for now
                android.util.Log.d("ContextAwareChatBot", "OllamaService response received")
                addMessage(ChatMessage(text = response, isUser = false))
            } catch (e: Exception) {
                android.util.Log.e("ContextAwareChatBot", "Error getting context or generating response", e)
                android.util.Log.e("ContextAwareChatBot", "Exception details: ${e.message}")
                android.util.Log.e("ContextAwareChatBot", "Stack trace: ${e.stackTraceToString()}")
                addMessage(
                    ChatMessage(
                        text = "I'm sorry, I'm having trouble connecting right now. Please try again later.",
                        isUser = false
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun getUserContext(): UserContext? {
        return try {
            android.util.Log.d("ContextAwareChatBot", "Starting getUserContext()")
            android.util.Log.d("ContextAwareChatBot", "Current user ID: $currentUserId")

            // Get today's data from the repository
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay()
            val endOfDay = today.plusDays(1).atStartOfDay()

            android.util.Log.d("ContextAwareChatBot", "Current date: $today")
            android.util.Log.d("ContextAwareChatBot", "Start of day: $startOfDay")
            android.util.Log.d("ContextAwareChatBot", "End of day: $endOfDay")

            // Get glucose logs
            val glucoseLogs = repository.getAllBloodGlucoseLogs().first()
            android.util.Log.d("ContextAwareChatBot", "Total glucose logs: ${glucoseLogs.size}")
            android.util.Log.d("ContextAwareChatBot", "Sample user IDs in glucose logs: ${glucoseLogs.take(3).map { it.userId }}")
            val todayGlucoseLogs = glucoseLogs.filter {
                it.userId == currentUserId && it.dateTime >= startOfDay && it.dateTime < endOfDay
            }
            android.util.Log.d("ContextAwareChatBot", "Today's glucose logs: ${todayGlucoseLogs.size}")
            val latestGlucose = todayGlucoseLogs.maxByOrNull { it.dateTime }?.value
            val glucoseReadings = todayGlucoseLogs.size
            val glucoseTrend = todayGlucoseLogs.map { it.value }

            // Get diet logs
            val dietLogs = repository.getAllDietLogs().first()
            android.util.Log.d("ContextAwareChatBot", "Total diet logs: ${dietLogs.size}")
            val todayDietLogs = dietLogs.filter {
                it.userId == currentUserId && it.dateTime >= startOfDay && it.dateTime < endOfDay
            }
            android.util.Log.d("ContextAwareChatBot", "Today's diet logs: ${todayDietLogs.size}")
            val todayCalories = todayDietLogs.sumOf { (it.calories ?: 0).toInt() }
            val mealCalories = MealCalories(
                breakfast = todayDietLogs.filter { it.period.toString() == "Breakfast" }.sumOf { (it.calories ?: 0).toInt() },
                lunch = todayDietLogs.filter { it.period.toString() == "Lunch" }.sumOf { (it.calories ?: 0).toInt() },
                dinner = todayDietLogs.filter { it.period.toString() == "Dinner" }.sumOf { (it.calories ?: 0).toInt() }
            )

            // Get exercise logs
            val exerciseLogs = repository.getAllExerciseLogs().first()
            android.util.Log.d("ContextAwareChatBot", "Total exercise logs: ${exerciseLogs.size}")
            val todayExerciseLogs = exerciseLogs.filter {
                it.userId == currentUserId && it.dateTime >= startOfDay && it.dateTime < endOfDay
            }
            android.util.Log.d("ContextAwareChatBot", "Today's exercise logs: ${todayExerciseLogs.size}")
            val exerciseMinutes = todayExerciseLogs.sumOf { it.durationMinutes }

            // Get medication logs
            val medicationLogs = repository.getAllMedicationLogs().first()
            android.util.Log.d("ContextAwareChatBot", "Total medication logs: ${medicationLogs.size}")
            val todayMedicationLogs = medicationLogs.filter {
                it.userId == currentUserId && it.dateTime >= startOfDay && it.dateTime < endOfDay
            }
            android.util.Log.d("ContextAwareChatBot", "Today's medication logs: ${todayMedicationLogs.size}")
            val medicationCount = todayMedicationLogs.size

            // Create user profile (you can enhance this with actual user data)
            val userProfile = UserProfile(
                name = "User", // Get from user profile
                age = 0, // Get from user profile
                diabetesType = "Type 2", // Get from user profile
                targetGlucose = 7.0f, // Get from user profile
                medications = todayMedicationLogs.map { it.name }.distinct()
            )

            val context = UserContext(
                latestGlucose = latestGlucose,
                glucoseTrend = glucoseTrend,
                todayCalories = todayCalories,
                exerciseMinutes = exerciseMinutes,
                medicationCount = medicationCount,
                glucoseReadings = glucoseReadings,
                mealCalories = mealCalories,
                userProfile = userProfile
            )

            android.util.Log.d("ContextAwareChatBot", "Built context: glucose=${latestGlucose}, calories=$todayCalories, exercise=$exerciseMinutes, meds=$medicationCount")
            android.util.Log.d("ContextAwareChatBot", "Context details: latestGlucose=$latestGlucose, glucoseReadings=$glucoseReadings, todayCalories=$todayCalories, exerciseMinutes=$exerciseMinutes, medicationCount=$medicationCount")

            // If no real data found, create a test context for debugging
            if (latestGlucose == null && todayCalories == 0 && exerciseMinutes == 0 && medicationCount == 0) {
                android.util.Log.d("ContextAwareChatBot", "No real data found, creating test context")
                val testContext = UserContext(
                    latestGlucose = 6.8f,
                    glucoseTrend = listOf(6.5f, 7.2f, 6.8f),
                    todayCalories = 1850,
                    exerciseMinutes = 30,
                    medicationCount = 2,
                    glucoseReadings = 3,
                    mealCalories = MealCalories(breakfast = 450, lunch = 600, dinner = 800),
                    userProfile = UserProfile(
                        name = "Test User",
                        age = 45,
                        diabetesType = "Type 2",
                        targetGlucose = 7.0f,
                        medications = listOf("Metformin", "Insulin")
                    )
                )
                android.util.Log.d("ContextAwareChatBot", "Test context created: $testContext")
                return testContext
            }

            context
        } catch (e: Exception) {
            android.util.Log.e("ContextAwareChatBot", "Error in getUserContext()", e)
            android.util.Log.e("ContextAwareChatBot", "Exception message: ${e.message}")
            android.util.Log.e("ContextAwareChatBot", "Exception stack trace: ${e.stackTraceToString()}")
            null
        }
    }

    private fun addMessage(message: ChatMessage) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + message
        )
    }

    private fun testConnection() {
        viewModelScope.launch {
            val isConnected = OllamaService.testConnection()
            _uiState.value = _uiState.value.copy(isConnected = isConnected)
        }
    }

    fun retryConnection() {
        testConnection()
    }
}
