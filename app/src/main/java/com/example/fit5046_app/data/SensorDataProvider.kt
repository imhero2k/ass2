package com.example.fit5046_app.data

import android.content.Context
import com.example.fit5046_app.model.EntryPeriod
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.LocalTime

class SensorDataProvider(private val context: Context) {
    private val refreshIntervalMs: Long = 10000 // 10 seconds

    /**
     * Reads glucose values from CSV and emits them as sensor readings every 10 seconds
     */
    fun sensorDataFlow(): Flow<BloodGlucoseLogEntity> = flow {
        val csvInputStream = context.assets.open("SmartSensor_DiabetesMonitoring_1200.csv")
        val lines = csvInputStream.bufferedReader().readLines()
        
        // Skip header line
        val dataLines = lines.drop(1)
        
        var index = 0
        while (true) {
            if (dataLines.isEmpty()) break
            
            // Cycle through the CSV data
            val line = dataLines[index % dataLines.size]
            val columns = line.split(",")
            
            // Get glucose value from second column (index 1)
            val glucoseValue = columns.getOrNull(1)?.toFloatOrNull()
            
            if (glucoseValue != null) {
                // Get current user ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
                
                // Create blood glucose log entity
                val currentTime = LocalDateTime.now()
                val reading = BloodGlucoseLogEntity(
                    userId = userId,
                    dateTime = currentTime,
                    period = getPeriodFromTime(currentTime.toLocalTime()),
                    notes = "From sensor reading",
                    value = glucoseValue
                )
                
                // Emit the reading (repository will save it)
                emit(reading)
            }
            
            index++
            delay(refreshIntervalMs)
        }
    }

    /**
     * Determines the period based on time of day
     */
    private fun getPeriodFromTime(time: LocalTime): EntryPeriod {
        return when (time.hour) {
            in 5..11 -> EntryPeriod.MORNING
            in 12..16 -> EntryPeriod.AFTERNOON
            in 17..20 -> EntryPeriod.EVENING
            else -> EntryPeriod.NIGHT
        }
    }
}
