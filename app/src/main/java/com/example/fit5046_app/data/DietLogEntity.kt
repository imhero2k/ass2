package com.example.fit5046_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fit5046_app.model.DietaryPreference
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.MealType
import java.time.LocalDateTime

@Entity(
    tableName = "diet_logs"
//    foreignKeys = [
//        ForeignKey(
//            entity = UserEntity::class,
//            parentColumns = ["id"],
//            childColumns = ["userId"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ],
//    indices = [Index("userId")]
)
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val dateTime: LocalDateTime,
    val period: EntryPeriod,
    val notes: String?,
    val mealType: MealType?,
    val dietaryPreference: List<DietaryPreference>?,
    val calories: Int?,
    val proteinGrams: Float?,
    val fatGrams: Float?,
    val carbGrams: Float?,
    val foodItems: List<String>?
)
