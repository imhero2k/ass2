package com.example.fit5046_app.model

import BloodGlucoseLogEntity
import DietLogEntity
import ExerciseLogEntity
import FoodItem
import MedicationLogEntity
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

    companion object
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

// ----- Log Item Models for Displaying in the Diary -----

sealed class LogDisplayItem {
    abstract val id: Int
    abstract val dateTime: LocalDateTime
    abstract val type: LogType
    abstract val period: EntryPeriod
    abstract val notes: String?

    data class DietItem(
        override val id: Int,
        override val dateTime: LocalDateTime,
        override val period: EntryPeriod,
        override val notes: String?,
        val calories: Int?,
        val proteinGrams: Float?,
        val fatGrams: Float?,
        val carbGrams: Float?,
        val foodItems: List<FoodItem>?
    ) : LogDisplayItem() {
        override val type = LogType.DIET
    }

    data class GlucoseItem(
        override val id: Int,
        override val dateTime: LocalDateTime,
        override val period: EntryPeriod,
        override val notes: String?,
        val value: Float
    ) : LogDisplayItem() {
        override val type = LogType.BLOOD_GLUCOSE
    }

    data class ExerciseItem(
        override val id: Int,
        override val dateTime: LocalDateTime,
        override val period: EntryPeriod,
        override val notes: String?,
        val exerciseType: ExerciseType,
        val durationMinutes: Int,
        val intensity: Intensity
    ) : LogDisplayItem() {
        override val type = LogType.EXERCISE
    }

    data class MedicationItem(
        override val id: Int,
        override val dateTime: LocalDateTime,
        override val period: EntryPeriod,
        override val notes: String?,
        val name: String,
        val dosage: Float,
        val dosageUnit: String
    ) : LogDisplayItem() {
        override val type = LogType.MEDICATION
    }
}

fun BloodGlucoseLogEntity.toDisplayItem(): LogDisplayItem = LogDisplayItem.GlucoseItem(
    id = id,
    dateTime = dateTime,
    period = period,
    notes = notes,
    value = value
)

fun MedicationLogEntity.toDisplayItem() = LogDisplayItem.MedicationItem(
    id = id,
    dateTime = dateTime,
    period = period,
    notes = notes,
    name = name,
    dosage = dosage,
    dosageUnit = dosageUnit
)

fun DietLogEntity.toDisplayItem(): LogDisplayItem = LogDisplayItem.DietItem(
    id = id,
    dateTime = dateTime,
    period = period,
    notes = notes,
    calories = calories,
    proteinGrams = proteinGrams,
    fatGrams = fatGrams,
    carbGrams = carbGrams,
    foodItems = foodItems
)

fun ExerciseLogEntity.toDisplayItem() = LogDisplayItem.ExerciseItem(
    id = id,
    dateTime = dateTime,
    period = period,
    notes = notes,
    exerciseType = exerciseType,
    durationMinutes = durationMinutes,
    intensity = intensity
)






