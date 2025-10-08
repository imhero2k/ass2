import androidx.room.TypeConverter
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.ExerciseType
import com.example.fit5046_app.model.Intensity
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
    fun fromFoodItemList(foodItems: List<FoodItem>?): String? {
        return gson.toJson(foodItems)
    }

    @TypeConverter
    fun toFoodItemList(data: String?): List<FoodItem>? {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<FoodItem>>() {}.type
        return gson.fromJson(data, type)
    }
}
