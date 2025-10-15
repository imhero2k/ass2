package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.fit5046_app.model.LogDisplayItem
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.SensorDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository
    private val sensorDataProvider: SensorDataProvider

    private val _sensorData = MutableStateFlow<BloodGlucoseLogEntity?>(null)
    val sensorData: StateFlow<BloodGlucoseLogEntity?> = _sensorData.asStateFlow()

    init {
        sensorDataProvider = SensorDataProvider(application.applicationContext)
        repository = LogRepository(application, sensorDataProvider)

        viewModelScope.launch {
            // Collect sensor readings and save to database
            repository.getSensorData().collect { reading ->
                _sensorData.value = reading
            }
        }
    }

    fun getAllLogs(): Flow<List<LogDisplayItem>> {
        return repository.getAllLogs()
    }

    fun getAllLogsForUser(userId: String): Flow<List<LogDisplayItem>> {
        return repository.getAllLogsForUser(userId)
    }
}
