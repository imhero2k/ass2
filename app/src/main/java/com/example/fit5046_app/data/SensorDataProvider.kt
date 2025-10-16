package com.example.fit5046_app.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime

class SensorDataProvider(private val context: Context) {
    private val refreshIntervalMs: Long = 10000 // 10 seconds

    /**
     * Reads glucose values from CSV and emits glucose readings with timestamp
     * Returns Flow of (timestamp, userId, glucoseValue) tuples
     */
    fun sensorDataFlow(): Flow<Triple<LocalDateTime, String, Float>> = flow {
        val csvInputStream = context.assets.open("SmartSensor_DiabetesMonitoring_1200.csv")
        val lines = csvInputStream.bufferedReader().readLines()
    
        val dataLines = lines.drop(1) // skip header
        
        var index = 0
        while (index < 24) {
            if (dataLines.isEmpty()) break
            val line = dataLines[index % dataLines.size] // cycle through the csv
            
            // Get glucose value from second column (index 1)
            val columns = line.split(",")
            val glucoseValue = columns.getOrNull(1)?.toFloatOrNull()
            
            if (glucoseValue != null) {
                // Get current user ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
                val currentTime = LocalDateTime.now()
                
                // Emit timestamp, user ID, and glucose value
                emit(Triple(currentTime, userId, glucoseValue))
            }
            index++
            delay(refreshIntervalMs)
        }
    }
}
