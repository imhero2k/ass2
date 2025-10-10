package com.example.fit5046_app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {
    // @Query("SELECT * FROM medication_logs WHERE userId = :userId ORDER BY dateTime DESC")
    // fun getAllLogs(userId: String): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs ORDER BY dateTime DESC")
    fun getAllLogs(): Flow<List<MedicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MedicationLogEntity)

    @Update
    suspend fun update(log: MedicationLogEntity)

    @Delete
    suspend fun delete(log: MedicationLogEntity)
}
