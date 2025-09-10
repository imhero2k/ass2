package com.example.fit5046_app.model

import java.time.LocalDateTime

enum class LogType(val displayName: String) {
    DIET("Diet"),
    BLOOD_GLUCOSE("Blood Glucose"),
    EXERCISE("Exercise"),
    MEDICATION("Medication")
}

enum class EntryPeriod(val displayName: String) {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    NIGHT("Night"),

    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    SNACK("Snack"),
    DINNER("Dinner"),

    BEFORE_BREAKFAST("Before Breakfast"),
    AFTER_BREAKFAST("After Breakfast"),
    BEFORE_LUNCH("Before Lunch"),
    AFTER_LUNCH("After Lunch"),
    BEFORE_DINNER("Before Dinner"),
    AFTER_DINNER("After Dinner"),
    BEFORE_SNACK("Before Snack"),
    AFTER_SNACK("After Snack"),
    BEFORE_EXERCISE("Before Exercise"),
    AFTER_EXERCISE("After Exercise"),
    BEFORE_MEDICATION("Before Medication"),
    AFTER_MEDICATION("After Medication"),

    OTHER("Other")
}

enum class ExerciseType(val displayName: String) {
    RUNNING("Running"),
    WALKING("Walking"),
    CYCLING("Cycling"),
    SWIMMING("Swimming"),
    YOGA("Yoga"),
    WEIGHT_TRAINING("Weight Training"),
    OTHER("Other")
}

enum class Intensity(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

sealed interface Log {
    val id: String
    val dateTime: LocalDateTime
    val period: EntryPeriod
    val type: LogType
    val notes: String?
}

data class FoodItemModel(
    val name: String,
    val quantity: String?
)

data class DietLog(
    override val id: String,
    override val dateTime: LocalDateTime,
    override val period: EntryPeriod,
    override val notes: String?,
    val calories: Int?,
    val proteinGrams: Float?,
    val fatGrams: Float?,
    val carbGrams: Float?,
    val foodItems: List<FoodItemModel>
) : Log {
    override val type: LogType get() = LogType.DIET
}

data class BloodGlucoseLog(
    override val id: String,
    override val dateTime: LocalDateTime,
    override val period: EntryPeriod,
    override val notes: String?,
    val value: Float
) : Log {
    override val type: LogType get() = LogType.BLOOD_GLUCOSE
}

data class ExerciseLog(
    override val id: String,
    override val dateTime: LocalDateTime,
    override val period: EntryPeriod,
    override val notes: String?,
    val durationMinutes: Int,
    val exerciseType: ExerciseType,
    val intensity: Intensity,
) : Log {
    override val type: LogType get() = LogType.EXERCISE
}

data class MedicationLog(
    override val id: String,
    override val dateTime: LocalDateTime,
    override val period: EntryPeriod,
    override val notes: String?,
    val name: String,
    val dosage: Float,
    val dosageUnit: String
) : Log {
    override val type: LogType get() = LogType.MEDICATION
}



