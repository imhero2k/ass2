package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.service.OllamaService
import com.example.fit5046_app.service.UserContext
import com.example.fit5046_app.service.UserProfile
import com.example.fit5046_app.service.MealCalories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val firestore = FirebaseFirestore.getInstance()
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

                val userContext = getUserContext()
                android.util.Log.d("ContextAwareChatBot", "User context returned: $userContext")
                android.util.Log.d("ContextAwareChatBot", "About to call OllamaService.generateResponse()")
                val response = OllamaService.generateResponse(currentInput, userContext)
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

            // Get diet logs with detailed food information
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

            // Get detailed food intake
            val foodIntakeDetails = todayDietLogs.map { log ->
                val foodItems = log.foodItems?.joinToString(", ") ?: "Unknown food"
                "• $foodItems (${log.calories ?: 0} cal) at ${log.dateTime.toLocalTime().toString().substring(0, 5)} - ${log.period}"
            }
            android.util.Log.d("ContextAwareChatBot", "Food intake details: ${foodIntakeDetails.size} items")

            // Get exercise logs with detailed information
            val exerciseLogs = repository.getAllExerciseLogs().first()
            android.util.Log.d("ContextAwareChatBot", "Total exercise logs: ${exerciseLogs.size}")
            val todayExerciseLogs = exerciseLogs.filter {
                it.userId == currentUserId && it.dateTime >= startOfDay && it.dateTime < endOfDay
            }
            android.util.Log.d("ContextAwareChatBot", "Today's exercise logs: ${todayExerciseLogs.size}")
            val exerciseMinutes = todayExerciseLogs.sumOf { it.durationMinutes }

            // Get detailed exercise information
            val exerciseDetails = todayExerciseLogs.map { log ->
                "• ${log.exerciseType} for ${log.durationMinutes} minutes at ${log.dateTime.toLocalTime().toString().substring(0, 5)} (${log.intensity} intensity)"
            }
            android.util.Log.d("ContextAwareChatBot", "Exercise details: ${exerciseDetails.size} activities")

            // Get medication logs
            val medicationLogs = repository.getAllMedicationLogs().first()
            android.util.Log.d("ContextAwareChatBot", "Total medication logs: ${medicationLogs.size}")
            val todayMedicationLogs = medicationLogs.filter {
                it.userId == currentUserId && it.dateTime >= startOfDay && it.dateTime < endOfDay
            }
            android.util.Log.d("ContextAwareChatBot", "Today's medication logs: ${todayMedicationLogs.size}")
            val medicationCount = todayMedicationLogs.size

            // Get detailed medication information
            val medicationDetails = todayMedicationLogs.map { log ->
                "• ${log.name} (${log.dosage}) at ${log.dateTime.toLocalTime().toString().substring(0, 5)}"
            }
            android.util.Log.d("ContextAwareChatBot", "Medication details: ${medicationDetails.size} doses")

            // Get user profile from Firestore
            val userProfile = getUserProfileFromFirestore()

            val context = UserContext(
                latestGlucose = latestGlucose,
                glucoseTrend = glucoseTrend,
                todayCalories = todayCalories,
                exerciseMinutes = exerciseMinutes,
                medicationCount = medicationCount,
                glucoseReadings = glucoseReadings,
                mealCalories = mealCalories,
                userProfile = userProfile,
                // Enhanced context with detailed information
                foodIntakeDetails = foodIntakeDetails,
                exerciseDetails = exerciseDetails,
                medicationDetails = medicationDetails
            )

            android.util.Log.d("ContextAwareChatBot", "Built context: glucose=${latestGlucose}, calories=$todayCalories, exercise=$exerciseMinutes, meds=$medicationCount")
            android.util.Log.d("ContextAwareChatBot", "Context details: latestGlucose=$latestGlucose, glucoseReadings=$glucoseReadings, todayCalories=$todayCalories, exerciseMinutes=$exerciseMinutes, medicationCount=$medicationCount")
            android.util.Log.d("ContextAwareChatBot", "Food details: ${foodIntakeDetails.size} items")
            android.util.Log.d("ContextAwareChatBot", "Exercise details: ${exerciseDetails.size} activities")
            android.util.Log.d("ContextAwareChatBot", "Medication details: ${medicationDetails.size} doses")

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

    private suspend fun getUserProfileFromFirestore(): UserProfile {
        return try {
            android.util.Log.d("ContextAwareChatBot", "Getting user profile from Firestore for user: $currentUserId")

            val userDoc = firestore.collection("users").document(currentUserId).get().await()

            if (userDoc.exists()) {
                val name = userDoc.getString("name") ?: "User"
                val age = userDoc.getLong("age")?.toInt() ?: 0
                val diabetesType = userDoc.getString("diabetesType") ?: "Type 2"
                val targetGlucose = userDoc.getDouble("targetGlucose")?.toFloat() ?: 7.0f
                val medications = userDoc.get("medications") as? List<String> ?: emptyList()

                android.util.Log.d("ContextAwareChatBot", "Firestore profile: name=$name, age=$age, diabetesType=$diabetesType, targetGlucose=$targetGlucose")

                UserProfile(
                    name = name,
                    age = age,
                    diabetesType = diabetesType,
                    targetGlucose = targetGlucose,
                    medications = medications
                )
            } else {
                android.util.Log.d("ContextAwareChatBot", "No user profile found in Firestore, using defaults")
                UserProfile(
                    name = "User",
                    age = 0,
                    diabetesType = "Type 2",
                    targetGlucose = 7.0f,
                    medications = emptyList()
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ContextAwareChatBot", "Error getting user profile from Firestore", e)
            UserProfile(
                name = "User",
                age = 0,
                diabetesType = "Type 2",
                targetGlucose = 7.0f,
                medications = emptyList()
            )
        }
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
