package com.example.fit5046_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fit5046_app.model.EntryPeriod
import java.time.LocalDateTime

@Entity(
    tableName = "medication_logs",
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
data class MedicationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val dateTime: LocalDateTime,
    val period: EntryPeriod,
    val notes: String?,
    val name: String,
    val dosage: Float,
    val dosageUnit: String
)
