import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.example.fit5046_app.model.LogDisplayItem
import com.example.fit5046_app.model.toDisplayItem

class LogRepository(application: Application) {
    private val userDao: UserDao = LogDatabase.getDatabase(application).userDao()
    private val dietLogDao: DietLogDao = LogDatabase.getDatabase(application).dietLogDao()
    private val bloodGlucoseLogDao: BloodGlucoseLogDao = LogDatabase.getDatabase(application).bloodGlucoseLogDao()
    // private val exerciseDao: ExerciseLogDao,
    // private val medicationDao: MedicationLogDao

    // ----- User Operations-----
    fun getUserById(userId: String) : Flow<UserEntity?> = userDao.getUser(userId)
    
    suspend fun insertUser(user: UserEntity) = userDao.insert(user)

    // ----- Diet Log Operations -----
    fun getAllDietLogs() : Flow<List<DietLogEntity>> = dietLogDao.getAllLogs()

    suspend fun insertDietLog(log: DietLogEntity) = dietLogDao.insert(log)

    // ----- Blood Glucose Log Operations -----
    fun getAllBloodGlucoseLogs() : Flow<List<BloodGlucoseLogEntity>> = bloodGlucoseLogDao.getAllLogs()

    suspend fun insertBloodGlucoseLog(log: BloodGlucoseLogEntity) = bloodGlucoseLogDao.insert(log)


    // ----- Combine all logs into one chronological feed -----
    fun getAllLogs(): Flow<List<LogDisplayItem>> {
        return combine(
            dietLogDao.getAllLogs(),
            bloodGlucoseLogDao.getAllLogs()
            // exerciseDao.getAll(),
            // medicationDao.getAll()
        ) { dietLogs, glucoseLogs ->
            val all = mutableListOf<LogDisplayItem>()
            all += dietLogs.map { it.toDisplayItem() }
            all += glucoseLogs.map { it.toDisplayItem() }
            // all += exerciseLogs.map { it.toDisplayItem() }
            // all += medicationLogs.map { it.toDisplayItem() }
            all.sortedByDescending { it.dateTime }
        }
    }
}
