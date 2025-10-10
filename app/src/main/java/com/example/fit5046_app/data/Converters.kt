package com.example.fit5046_app.data

import androidx.room.TypeConverter
import com.example.fit5046_app.model.DietaryPreference
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.ExerciseType
import com.example.fit5046_app.model.Intensity
import com.example.fit5046_app.model.MealType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDateTime(millis: Long?): LocalDateTime? {
        return millis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
    }

    @TypeConverter
    fun fromEntryPeriod(value: EntryPeriod): String = value.name

    @TypeConverter
    fun toEntryPeriod(value: String): EntryPeriod = EntryPeriod.valueOf(value)

    @TypeConverter
    fun fromExerciseType(value: ExerciseType): String = value.name

    @TypeConverter
    fun toExerciseType(value: String): ExerciseType = ExerciseType.valueOf(value)

    @TypeConverter
    fun fromIntensity(value: Intensity): String = value.name

    @TypeConverter
    fun toIntensity(value: String): Intensity = Intensity.valueOf(value)

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun fromMealType(value: MealType?): String? = value?.name

    @TypeConverter
    fun toMealType(value: String?): MealType? = value?.let { MealType.valueOf(it) }

    @TypeConverter
    fun fromDietaryPreferenceList(list: List<DietaryPreference>?): String? {
        return list?.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toDietaryPreferenceList(data: String?): List<DietaryPreference>? {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").map { DietaryPreference.valueOf(it) }
    }
}
