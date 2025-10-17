package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.fit5046_app.model.LogDisplayItem
import kotlinx.coroutines.flow.Flow


class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    // Get repository from application class (single instance, sensor already running)
    private val repository: LogRepository = 
        (application as GlucoseDiaryApplication).logRepository

    fun getAllLogs(): Flow<List<LogDisplayItem>> {
        return repository.getAllLogs()
    }

    fun getAllLogsForUser(userId: String): Flow<List<LogDisplayItem>> {
        return repository.getAllLogsForUser(userId)
    }
}
