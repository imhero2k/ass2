package com.example.fit5046_app

import android.app.Application
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.DietLogEntity
import com.example.fit5046_app.data.ExerciseLogEntity
import com.example.fit5046_app.data.MedicationLogEntity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository

    init {
        repository = LogRepository(application)
    }

    fun insertDietLog(dietLog: DietLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertDietLog(dietLog)
    }

    fun insertBloodGlucoseLog(bloodGlucoseLog: BloodGlucoseLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBloodGlucoseLog(bloodGlucoseLog)
        }

    fun insertExerciseLog(exerciseLog: ExerciseLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertExerciseLog(exerciseLog)
    }

    fun insertMedicationLog(medicationLog: MedicationLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMedicationLog(medicationLog)
        }


}