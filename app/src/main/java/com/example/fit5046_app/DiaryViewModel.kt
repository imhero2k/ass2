package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.fit5046_app.model.LogDisplayItem
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository = LogRepository(application)

    private val _sensorData = MutableStateFlow<BloodGlucoseLogEntity?>(null)
    val sensorData: StateFlow<BloodGlucoseLogEntity?> = _sensorData.asStateFlow()

    init {
        // Start collecting sensor data
        // Note: collecting in Diary viewmodel scope instead of application scope or running in the background is
        // to avoid crowding ROOM and the screen, because sensor simulator emits data very fast for demo purposes.
        viewModelScope.launch {
            repository.getSensorDataFlow().collect { reading ->
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
