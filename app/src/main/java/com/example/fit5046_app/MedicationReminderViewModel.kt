package com.example.fit5046_app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.data.MedicationReminderDao
import com.example.fit5046_app.data.MedicationReminderEntity
import com.example.fit5046_app.model.*
import com.example.fit5046_app.service.MedicationReminderService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

data class MedicationReminderState(
    val isLoading: Boolean = false,
    val reminders: List<MedicationReminder> = emptyList(),
    val errorMessage: String? = null
)

class MedicationReminderViewModel(
    private val reminderDao: MedicationReminderDao
) : ViewModel() {
    private val _reminderState = MutableStateFlow(MedicationReminderState())
    val reminderState: StateFlow<MedicationReminderState> = _reminderState.asStateFlow()

    fun getAllReminders(): Flow<List<MedicationReminder>> {
        return reminderDao.getAllReminders().map { entities ->
            entities.map { it.toMedicationReminder() }
        }
    }

    fun getActiveReminders(): Flow<List<MedicationReminder>> {
        return reminderDao.getActiveReminders().map { entities ->
            entities.map { it.toMedicationReminder() }
        }
    }

    fun addReminder(context: Context, reminder: MedicationReminder) {
        viewModelScope.launch {
            try {
                _reminderState.value = _reminderState.value.copy(isLoading = true, errorMessage = null)

                val entity = reminder.toMedicationReminderEntity()
                reminderDao.insertReminder(entity)

                // Schedule the reminder with AlarmManager
                MedicationReminderService.scheduleReminder(context, reminder)

                _reminderState.value = _reminderState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _reminderState.value = _reminderState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to add reminder"
                )
            }
        }
    }

    fun updateReminder(context: Context, reminder: MedicationReminder) {
        viewModelScope.launch {
            try {
                _reminderState.value = _reminderState.value.copy(isLoading = true, errorMessage = null)

                // Cancel existing reminder
                MedicationReminderService.cancelReminder(context, reminder.id)

                val entity = reminder.toMedicationReminderEntity()
                reminderDao.updateReminder(entity)

                // Schedule updated reminder
                if (reminder.isActive) {
                    MedicationReminderService.scheduleReminder(context, reminder)
                }

                _reminderState.value = _reminderState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _reminderState.value = _reminderState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to update reminder"
                )
            }
        }
    }

    fun deleteReminder(context: Context, reminderId: String) {
        viewModelScope.launch {
            try {
                _reminderState.value = _reminderState.value.copy(isLoading = true, errorMessage = null)

                // Cancel the reminder
                MedicationReminderService.cancelReminder(context, reminderId)

                reminderDao.deleteReminderById(reminderId)

                _reminderState.value = _reminderState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _reminderState.value = _reminderState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to delete reminder"
                )
            }
        }
    }

    fun toggleReminderStatus(context: Context, reminderId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                _reminderState.value = _reminderState.value.copy(isLoading = true, errorMessage = null)

                reminderDao.updateReminderStatus(reminderId, isActive)

                if (!isActive) {
                    MedicationReminderService.cancelReminder(context, reminderId)
                } else {
                    // Get the reminder and reschedule it
                    val entity = reminderDao.getReminderById(reminderId)
                    entity?.let {
                        val reminder = it.toMedicationReminder()
                        MedicationReminderService.scheduleReminder(context, reminder)
                    }
                }

                _reminderState.value = _reminderState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _reminderState.value = _reminderState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to toggle reminder status"
                )
            }
        }
    }

    fun clearError() {
        _reminderState.value = _reminderState.value.copy(errorMessage = null)
    }
}

// Extension functions for conversion
private fun MedicationReminderEntity.toMedicationReminder(): MedicationReminder {
    val gson = Gson()
    return MedicationReminder(
        id = id,
        medicationName = medicationName,
        dosage = dosage,
        reminderType = ReminderType.valueOf(reminderType),
        timeOfDay = TimeOfDay.valueOf(timeOfDay),
        hour = hour,
        minute = minute,
        isActive = isActive,
        daysOfWeek = gson.fromJson(daysOfWeek, Array<ReminderDayOfWeek>::class.java).toList(),
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt)
    )
}

private fun MedicationReminder.toMedicationReminderEntity(): MedicationReminderEntity {
    val gson = Gson()
    return MedicationReminderEntity(
        id = id,
        medicationName = medicationName,
        dosage = dosage,
        reminderType = reminderType.name,
        timeOfDay = timeOfDay.name,
        hour = hour,
        minute = minute,
        isActive = isActive,
        daysOfWeek = gson.toJson(daysOfWeek),
        createdAt = createdAt.time,
        updatedAt = updatedAt.time
    )
}
