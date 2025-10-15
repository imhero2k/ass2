package com.example.fit5046_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "medication_reminders")
data class MedicationReminderEntity(
    @PrimaryKey
    val id: String,
    val medicationName: String,
    val dosage: String,
    val reminderType: String, // ReminderType enum as string
    val timeOfDay: String, // TimeOfDay enum as string
    val hour: Int,
    val minute: Int,
    val isActive: Boolean,
    val daysOfWeek: String, // JSON string of ReminderDayOfWeek list
    val createdAt: Long, // Date as timestamp
    val updatedAt: Long // Date as timestamp
)
