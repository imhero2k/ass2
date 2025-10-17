package com.example.fit5046_app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GlucoseDiaryApplication : Application() {
    // Application-wide coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Single instance of LogRepository for the entire app
    val logRepository: LogRepository by lazy {
        LogRepository(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Start sensor data collection at application scope.
        // Keep collecting continuously throughout the app lifecycle
        applicationScope.launch {
            logRepository.getSensorDataFlow().collect { reading ->
                // Sensor readings are automatically saved to database in getSensorDataFlow()
            }
        }
    }
}

