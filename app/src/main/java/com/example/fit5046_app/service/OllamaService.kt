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

class OllamaService {
    companion object {
        private const val TAG = "OllamaService"
        private const val OLLAMA_BASE_URL = "http://10.0.2.2:11434"
        private const val MODEL_NAME = "hf.co/mradermacher/Diabetica-1.5B-GGUF:Q4_K_M"

        suspend fun generateResponse(userMessage: String): String {
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
                    val contextualPrompt = createContextualPrompt(userMessage)

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
                        getFallbackResponse(userMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling Ollama API", e)
                    Log.e(TAG, "Exception details: ${e.message}")
                    Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                    getFallbackResponse(userMessage)
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

        private fun createContextualPrompt(userMessage: String): String {
            return """
                You are Diabetica, a specialized AI assistant for diabetes management and health support. 
                You help users with medication reminders, blood sugar monitoring, diet advice, exercise recommendations, 
                and general diabetes care. Be supportive, informative, and encouraging.
                
                User's message: $userMessage
                
                Please provide a helpful, personalized response focused on diabetes management and health.
            """.trimIndent()
        }

        private fun getFallbackResponse(userMessage: String): String {
            val lowerMessage = userMessage.lowercase()

            return when {
                lowerMessage.contains("medication") || lowerMessage.contains("reminder") -> {
                    "I can help you manage your medication reminders! You can set up reminders for insulin, tablets, and other medications. Would you like me to guide you through setting up a new reminder?"
                }
                lowerMessage.contains("insulin") -> {
                    "For insulin management, it's important to take your doses at the same time each day. You can set up multiple reminders throughout the day. What time do you usually take your insulin?"
                }
                lowerMessage.contains("blood sugar") || lowerMessage.contains("glucose") -> {
                    "Monitoring your blood sugar is crucial for diabetes management. The app can help you track your glucose levels and set reminders for testing. What's your target range?"
                }
                lowerMessage.contains("exercise") || lowerMessage.contains("workout") -> {
                    "Regular exercise is great for managing diabetes! Even a 30-minute walk can help improve your blood sugar levels. What type of exercise do you enjoy?"
                }
                lowerMessage.contains("diet") || lowerMessage.contains("food") || lowerMessage.contains("meal") -> {
                    "A balanced diet is key to managing diabetes. Focus on whole grains, lean proteins, and plenty of vegetables. Are you looking for meal planning tips?"
                }
                lowerMessage.contains("help") -> {
                    "I'm here to help! I can assist with medication reminders, health tracking, exercise tips, and general diabetes management advice. What would you like to know more about?"
                }
                else -> {
                    "That's a great question! I'm here to help with your health journey. I can assist with medication reminders, blood sugar tracking, exercise tips, and general health advice. Is there something specific you'd like to know about?"
                }
            }
        }
    }
}
