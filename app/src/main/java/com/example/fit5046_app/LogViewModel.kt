package com.example.fit5046_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.DietLogEntity
import com.example.fit5046_app.data.ExerciseLogEntity
import com.example.fit5046_app.data.MedicationLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository

    init {
        repository = LogRepository(application)
    }

    suspend fun getDietLogById(logId: Int): DietLogEntity? {
        return repository.getDietLogById(logId)
    }

    fun updateDietLog(dietLog: DietLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateDietLog(dietLog)
    }

    fun deleteDietLog(dietLog: DietLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteDietLog(dietLog)
    }

    fun insertDietLog(dietLog: DietLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertDietLog(dietLog)
    }

    suspend fun getBloodGlucoseLogById(logId: Int): BloodGlucoseLogEntity? {
        return repository.getBloodGlucoseLogById(logId)
    }

    fun insertBloodGlucoseLog(bloodGlucoseLog: BloodGlucoseLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBloodGlucoseLog(bloodGlucoseLog)
        }

    fun updateBloodGlucoseLog(bloodGlucoseLog: BloodGlucoseLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBloodGlucoseLog(bloodGlucoseLog)
        }

    fun deleteBloodGlucoseLog(bloodGlucoseLog: BloodGlucoseLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBloodGlucoseLog(bloodGlucoseLog)
        }

    fun deleteSensorGeneratedGlucoseLogs(userId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSensorGeneratedGlucoseLogs(userId)
        }

    suspend fun getExerciseLogById(logId: Int): ExerciseLogEntity? {
        return repository.getExerciseLogById(logId)
    }

    fun insertExerciseLog(exerciseLog: ExerciseLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertExerciseLog(exerciseLog)
    }

    fun updateExerciseLog(exerciseLog: ExerciseLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateExerciseLog(exerciseLog)
    }

    fun deleteExerciseLog(exerciseLog: ExerciseLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteExerciseLog(exerciseLog)
    }

    suspend fun getMedicationLogById(logId: Int): MedicationLogEntity? {
        return repository.getMedicationLogById(logId)
    }

    fun insertMedicationLog(medicationLog: MedicationLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMedicationLog(medicationLog)
        }

    fun updateMedicationLog(medicationLog: MedicationLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMedicationLog(medicationLog)
        }

    fun deleteMedicationLog(medicationLog: MedicationLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMedicationLog(medicationLog)
        }
}