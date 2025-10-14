package com.example.fit5046_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders ORDER BY hour, minute ASC")
    fun getAllReminders(): Flow<List<MedicationReminderEntity>>

    @Query("SELECT * FROM medication_reminders WHERE isActive = 1 ORDER BY hour, minute ASC")
    fun getActiveReminders(): Flow<List<MedicationReminderEntity>>

    @Query("SELECT * FROM medication_reminders WHERE id = :id")
    suspend fun getReminderById(id: String): MedicationReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminderEntity)

    @Update
    suspend fun updateReminder(reminder: MedicationReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: MedicationReminderEntity)

    @Query("DELETE FROM medication_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: String)

    @Query("UPDATE medication_reminders SET isActive = :isActive WHERE id = :id")
    suspend fun updateReminderStatus(id: String, isActive: Boolean)
}
