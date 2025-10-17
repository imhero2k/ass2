package com.example.fit5046_app.service

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

data class OllamaResponse(
    val model: String,
    @SerializedName("created_at")
    val createdAt: String,
    val response: String,
    val done: Boolean
)

data class UserContext(
    val latestGlucose: Float? = null,
    val glucoseTrend: List<Float> = emptyList(),
    val todayCalories: Int = 0,
    val exerciseMinutes: Int = 0,
    val medicationCount: Int = 0,
    val glucoseReadings: Int = 0,
    val mealCalories: MealCalories = MealCalories(),
    val userProfile: UserProfile? = null,
    // Enhanced context with detailed information
    val foodIntakeDetails: List<String> = emptyList(),
    val exerciseDetails: List<String> = emptyList(),
    val medicationDetails: List<String> = emptyList()
) // Enhanced UserContext with detailed information

data class MealCalories(
    val breakfast: Int = 0,
    val lunch: Int = 0,
    val dinner: Int = 0
)

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val diabetesType: String = "",
    val targetGlucose: Float = 7.0f,
    val medications: List<String> = emptyList()
)

class OllamaService {
    companion object {
        private const val TAG = "OllamaService"
        private const val OLLAMA_BASE_URL = "http://10.0.2.2:11434"
        private const val MODEL_NAME = "hf.co/mradermacher/Diabetica-1.5B-GGUF:Q4_K_M"

        suspend fun generateResponse(userMessage: String, userContext: UserContext? = null): String {
            return withContext(Dispatchers.IO) {
                var connection: HttpURLConnection? = null
                try {
                    val url = URL("$OLLAMA_BASE_URL/api/generate")
                    Log.d(TAG, "Attempting to connect to: $url")

                    connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000 // 10 seconds timeout
                    connection.readTimeout = 30000 // 30 seconds timeout

                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true
                    connection.doInput = true

                    // Create the prompt with context for diabetes management
                    val contextualPrompt = createContextualPrompt(userMessage, userContext)

                    val request = OllamaRequest(
                        model = MODEL_NAME,
                        prompt = contextualPrompt,
                        stream = false
                    )

                    val gson = Gson()
                    val jsonRequest = gson.toJson(request)

                    Log.d(TAG, "Sending request to Ollama: $jsonRequest")

                    // Send request
                    val outputStream = connection.outputStream
                    val writer = OutputStreamWriter(outputStream)
                    writer.write(jsonRequest)
                    writer.flush()
                    writer.close()

                    // Read response
                    val responseCode = connection.responseCode
                    Log.d(TAG, "Ollama response code: $responseCode")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = StringBuilder()

                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }

                        reader.close()
                        inputStream.close()

                        val jsonResponse = response.toString()
                        Log.d(TAG, "Ollama response: $jsonResponse")

                        val ollamaResponse = gson.fromJson(jsonResponse, OllamaResponse::class.java)
                        ollamaResponse.response
                    } else {
                        Log.e(TAG, "Error response from Ollama: $responseCode")
                        getFallbackResponse(userMessage, userContext)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling Ollama API", e)
                    Log.e(TAG, "Exception details: ${e.message}")
                    Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                    getFallbackResponse(userMessage, userContext)
                } finally {
                    connection?.disconnect()
                }
            }
        }

        suspend fun testConnection(): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val url = URL("$OLLAMA_BASE_URL/api/tags")
                    Log.d(TAG, "Testing connection to: $url")

                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000
                    connection.requestMethod = "GET"

                    val responseCode = connection.responseCode
                    Log.d(TAG, "Connection test response code: $responseCode")

                    connection.disconnect()
                    responseCode == HttpURLConnection.HTTP_OK
                } catch (e: Exception) {
                    Log.e(TAG, "Connection test failed", e)
                    false
                }
            }
        }

        private fun createContextualPrompt(userMessage: String, userContext: UserContext?): String {
            Log.d(TAG, "Creating prompt with context: $userContext")
            val contextInfo = if (userContext != null) {
                buildString {
                    appendLine("=== USER HEALTH CONTEXT ===")

                    // Glucose information
                    if (userContext.latestGlucose != null) {
                        appendLine("Latest Blood Glucose: ${String.format("%.1f", userContext.latestGlucose)} mmol/L")
                    }
                    if (userContext.glucoseReadings > 0) {
                        appendLine("Total Glucose Readings Today: ${userContext.glucoseReadings}")
                    }

                    // Meal information
                    if (userContext.todayCalories > 0) {
                        appendLine("Today's Calories: ${userContext.todayCalories} cal")
                    }
                    if (userContext.mealCalories.breakfast > 0 || userContext.mealCalories.lunch > 0 || userContext.mealCalories.dinner > 0) {
                        appendLine("Meal Breakdown - Breakfast: ${userContext.mealCalories.breakfast} cal, Lunch: ${userContext.mealCalories.lunch} cal, Dinner: ${userContext.mealCalories.dinner} cal")
                    }

                    // Exercise information
                    if (userContext.exerciseMinutes > 0) {
                        appendLine("Today's Exercise: ${userContext.exerciseMinutes} minutes")
                    }
                    if (userContext.exerciseDetails.isNotEmpty()) {
                        appendLine("Exercise Details:")
                        userContext.exerciseDetails.forEach { detail ->
                            appendLine("  $detail")
                        }
                    }

                    // Food intake details
                    if (userContext.foodIntakeDetails.isNotEmpty()) {
                        appendLine("Food Intake Today:")
                        userContext.foodIntakeDetails.forEach { detail ->
                            appendLine("  $detail")
                        }
                    }

                    // Medication information
                    if (userContext.medicationCount > 0) {
                        appendLine("Medications Taken Today: ${userContext.medicationCount} doses")
                    }
                    if (userContext.medicationDetails.isNotEmpty()) {
                        appendLine("Medication Details:")
                        userContext.medicationDetails.forEach { detail ->
                            appendLine("  $detail")
                        }
                    }

                    // User profile information
                    userContext.userProfile?.let { profile ->
                        if (profile.name.isNotEmpty()) {
                            appendLine("Patient Name: ${profile.name}")
                        }
                        if (profile.age > 0) {
                            appendLine("Age: ${profile.age} years")
                        }
                        if (profile.diabetesType.isNotEmpty()) {
                            appendLine("Diabetes Type: ${profile.diabetesType}")
                        }
                        if (profile.targetGlucose > 0) {
                            appendLine("Target Glucose: ${String.format("%.1f", profile.targetGlucose)} mmol/L")
                        }
                        if (profile.medications.isNotEmpty()) {
                            appendLine("Current Medications: ${profile.medications.joinToString(", ")}")
                        }
                    }

                    appendLine("=== END CONTEXT ===")
                    appendLine()
                }
            } else {
                ""
            }

            val prompt = """
                You are Diabetica, a specialized AI assistant for diabetes management and health support. 
                You help users with medication reminders, blood sugar monitoring, diet advice, exercise recommendations, 
                and general diabetes care. Be supportive, informative, and encouraging.
                
                $contextInfo
                
                User's message: $userMessage
                
                Please provide a helpful, personalized response focused on diabetes management and health. 
                Use the provided health context to give more relevant and personalized advice.
            """.trimIndent()

            Log.d(TAG, "Final prompt: $prompt")
            return prompt
        }

        private fun getFallbackResponse(userMessage: String, userContext: UserContext?): String {
            val lowerMessage = userMessage.lowercase()
            val contextInfo = if (userContext != null) {
                buildString {
                    if (userContext.latestGlucose != null) {
                        append(" Your latest glucose reading is ${String.format("%.1f", userContext.latestGlucose)} mmol/L.")
                    }
                    if (userContext.todayCalories > 0) {
                        append(" You've consumed ${userContext.todayCalories} calories today.")
                    }
                    if (userContext.exerciseMinutes > 0) {
                        append(" You've exercised for ${userContext.exerciseMinutes} minutes today.")
                    }
                }
            } else ""

            return when {
                lowerMessage.contains("medication") || lowerMessage.contains("reminder") -> {
                    "I can help you manage your medication reminders! You can set up reminders for insulin, tablets, and other medications.$contextInfo Would you like me to guide you through setting up a new reminder?"
                }
                lowerMessage.contains("insulin") -> {
                    "For insulin management, it's important to take your doses at the same time each day.$contextInfo You can set up multiple reminders throughout the day. What time do you usually take your insulin?"
                }
                lowerMessage.contains("blood sugar") || lowerMessage.contains("glucose") -> {
                    "Monitoring your blood sugar is crucial for diabetes management.$contextInfo The app can help you track your glucose levels and set reminders for testing. What's your target range?"
                }
                lowerMessage.contains("exercise") || lowerMessage.contains("workout") -> {
                    "Regular exercise is great for managing diabetes!$contextInfo Even a 30-minute walk can help improve your blood sugar levels. What type of exercise do you enjoy?"
                }
                lowerMessage.contains("diet") || lowerMessage.contains("food") || lowerMessage.contains("meal") -> {
                    "A balanced diet is key to managing diabetes.$contextInfo Focus on whole grains, lean proteins, and plenty of vegetables. Are you looking for meal planning tips?"
                }
                lowerMessage.contains("help") -> {
                    "I'm here to help!$contextInfo I can assist with medication reminders, health tracking, exercise tips, and general diabetes management advice. What would you like to know more about?"
                }
                else -> {
                    "That's a great question!$contextInfo I'm here to help with your health journey. I can assist with medication reminders, blood sugar tracking, exercise tips, and general health advice. Is there something specific you'd like to know about?"
                }
            }
        }
    }
}