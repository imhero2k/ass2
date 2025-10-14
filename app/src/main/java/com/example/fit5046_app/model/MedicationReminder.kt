package com.example.fit5046_app.model

import java.util.Date

data class MedicationReminder(
    val id: String = "",
    val medicationName: String = "",
    val dosage: String = "",
    val reminderType: ReminderType = ReminderType.INSULIN,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val hour: Int = 8,
    val minute: Int = 0,
    val isActive: Boolean = true,
    val daysOfWeek: List<ReminderDayOfWeek> = listOf(ReminderDayOfWeek.MONDAY, ReminderDayOfWeek.TUESDAY, ReminderDayOfWeek.WEDNESDAY, ReminderDayOfWeek.THURSDAY, ReminderDayOfWeek.FRIDAY, ReminderDayOfWeek.SATURDAY, ReminderDayOfWeek.SUNDAY),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class ReminderType {
    INSULIN,
    TABLET,
    INJECTION,
    OTHER
}

enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT
}

enum class ReminderDayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

data class ReminderNotification(
    val id: String,
    val medicationName: String,
    val dosage: String,
    val timeOfDay: TimeOfDay,
    val scheduledTime: Date,
    val isCompleted: Boolean = false
)
