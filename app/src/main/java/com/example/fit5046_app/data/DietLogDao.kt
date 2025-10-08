import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DietLogDao {
    // @Query("SELECT * FROM diet_logs WHERE userId = :userId ORDER BY dateTime DESC")
    // fun getAllLogs(userId: String): Flow<List<DietLogEntity>>

    @Query("SELECT * FROM diet_logs ORDER BY dateTime DESC")
    fun getAllLogs(): Flow<List<DietLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DietLogEntity)

    @Update
    suspend fun update(log: DietLogEntity)

    @Delete
    suspend fun delete(log: DietLogEntity)
}
