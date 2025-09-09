package com.example.fit5046_app

import com.example.fit5046_app.model.BloodGlucoseLog
import com.example.fit5046_app.model.DietLog
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.ExerciseLog
import com.example.fit5046_app.model.ExerciseType
import com.example.fit5046_app.model.FoodItemModel
import com.example.fit5046_app.model.Intensity
import com.example.fit5046_app.model.Log
import com.example.fit5046_app.model.MedicationLog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun getMockLogs(): List<Log> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return listOf(
        // --- Yesterday's Logs ---
        DietLog(
            id = "diet_yesterday_1",
            dateTime = LocalDateTime.of(
                yesterday,
                LocalTime.of(8, 15)
            ),
            period = EntryPeriod.MORNING,
            notes = "Breakfast: Oatmeal and fruit.",
            calories = 450,
            proteinGrams = 15f,
            fatGrams = 10f,
            carbGrams = 70f,
            foodItems = listOf(
                FoodItemModel(name = "Oatmeal", quantity = "1 cup"),
                FoodItemModel(name = "Berries", quantity = "1/2 cup")
            )
        ),
        BloodGlucoseLog(
            id = "bg_yesterday_1",
            dateTime = LocalDateTime.of(
                yesterday,
                LocalTime.of(8, 45)
            ),
            period = EntryPeriod.AFTER_BREAKFAST,
            notes = "Feeling good.",
            value = 6.2f
        ),
        ExerciseLog(
            id = "exercise_yesterday_1",
            dateTime = LocalDateTime.of(
                yesterday,
                LocalTime.of(17, 30)
            ),
            period = EntryPeriod.EVENING,
            notes = "Evening run.",
            durationMinutes = 30,
            exerciseType = ExerciseType.RUNNING,
            intensity = Intensity.MEDIUM
        ),
        MedicationLog(
            id = "med_yesterday_1",
            dateTime = LocalDateTime.of(
                yesterday,
                LocalTime.of(20, 0)
            ),
            period = EntryPeriod.AFTER_DINNER,
            notes = "Evening medication.",
            name = "Metformin",
            dosage = 500f,
            dosageUnit = "mg"
        ),

        // --- Today's Logs ---
        MedicationLog(
            id = "med_today_1",
            dateTime = LocalDateTime.of(
                today,
                LocalTime.of(7, 30)
            ),
            period = EntryPeriod.BEFORE_BREAKFAST,
            notes = "Morning meds.",
            name = "Vitamin D",
            dosage = 1000f,
            dosageUnit = "IU"
        ),
        DietLog(
            id = "diet_today_1",
            dateTime = LocalDateTime.of(
                today,
                LocalTime.of(12, 30)
            ),
            period = EntryPeriod.LUNCH,
            notes = "Quick lunch.",
            calories = 600,
            proteinGrams = 30f,
            fatGrams = 20f,
            carbGrams = 55f,
            foodItems = listOf(
                FoodItemModel(name = "Chicken Sandwich", quantity = "1"),
                FoodItemModel(name = "Apple", quantity = "1 medium")
            )
        ),
        BloodGlucoseLog(
            id = "bg_today_1",
            dateTime = LocalDateTime.of(
                today,
                LocalTime.of(14, 0)
            ),
            period = EntryPeriod.AFTER_LUNCH,
            notes = null,
            value = 5.8f
        ),
        ExerciseLog(
            id = "exercise_today_1",
            dateTime = LocalDateTime.of(
                today,
                LocalTime.of(9, 0)
            ),
            period = EntryPeriod.MORNING,
            notes = "Morning Yoga session.",
            durationMinutes = 45,
            exerciseType = ExerciseType.YOGA,
            intensity = Intensity.LOW
        )
    ).sortedByDescending { it.dateTime }
}