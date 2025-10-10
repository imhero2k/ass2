package com.example.fit5046_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.ExerciseType
import com.example.fit5046_app.model.Intensity
import java.time.LocalDateTime

@Entity(
    tableName = "exercise_logs",
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
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val dateTime: LocalDateTime,
    val period: EntryPeriod,
    val notes: String?,
    val durationMinutes: Int,
    val exerciseType: ExerciseType,
    val intensity: Intensity
)
