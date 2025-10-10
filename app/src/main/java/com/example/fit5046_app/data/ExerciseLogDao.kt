package com.example.fit5046_app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    // @Query("SELECT * FROM exercise_logs WHERE userId = :userId ORDER BY dateTime DESC")
    // fun getAllLogs(userId: String): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_logs ORDER BY dateTime DESC")
    fun getAllLogs(): Flow<List<ExerciseLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ExerciseLogEntity)

    @Update
    suspend fun update(log: ExerciseLogEntity)

    @Delete
    suspend fun delete(log: ExerciseLogEntity)
}
