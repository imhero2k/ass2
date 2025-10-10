package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.fit5046_app.model.LogDisplayItem
import kotlinx.coroutines.flow.Flow


class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository

    // private val _uiState = MutableStateFlow(DiaryUiState())
    // val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        repository = LogRepository(application)
    }

    fun getAllLogs(): Flow<List<LogDisplayItem>> {
        return repository.getAllLogs()
    }

    // private fun observeLogs() {
    //     viewModelScope.launch {
    //         repository.getAllLogs()
    //             .onStart { _uiState.update { it.copy(isLoading = true) } }
    //             .catch { e ->
    //                 _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
    //             }
    //             .collect { logs ->
    //                 _uiState.update {
    //                     it.copy(
    //                         isLoading = false,
    //                         logs = logs,
    //                         errorMessage = null
    //                     )
    //                 }
    //             }
    //     }
    // }

    // fun onDeleteLog(log: LogDisplayItem) {
    //     viewModelScope.launch {
    //         repository.deleteLog(log)
    //     }
    // }

    // fun onSelectLog(log: LogDisplayItem) {
    //     _uiState.update { it.copy(selectedLog = log) }
    // }

    // fun clearSelection() {
    //     _uiState.update { it.copy(selectedLog = null) }
    // }
}
