package com.example.fit5046_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodGlucoseLogDao {
    @Query("SELECT * FROM blood_glucose_logs WHERE userId = :userId ORDER BY dateTime DESC")
    fun getAllLogsForUser(userId: String): Flow<List<BloodGlucoseLogEntity>>

    @Query("SELECT * FROM blood_glucose_logs ORDER BY dateTime DESC")
    fun getAllLogs(): Flow<List<BloodGlucoseLogEntity>>

    @Insert
    suspend fun insert(log: BloodGlucoseLogEntity)

    @Update
    suspend fun update(log: BloodGlucoseLogEntity)

    @Delete
    suspend fun delete(log: BloodGlucoseLogEntity)
}