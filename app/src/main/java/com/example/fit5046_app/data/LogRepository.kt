package com.example.fit5046_app

import android.app.Application
import com.example.fit5046_app.data.BloodGlucoseLogDao
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.DietLogDao
import com.example.fit5046_app.data.DietLogEntity
import com.example.fit5046_app.data.ExerciseLogDao
import com.example.fit5046_app.data.ExerciseLogEntity
import com.example.fit5046_app.data.LogDatabase
import com.example.fit5046_app.data.MedicationLogDao
import com.example.fit5046_app.data.MedicationLogEntity
import com.example.fit5046_app.data.UserDao
import com.example.fit5046_app.data.UserEntity
import com.example.fit5046_app.model.LogDisplayItem
import com.example.fit5046_app.model.toDisplayItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LogRepository(application: Application) {
    private val userDao: UserDao = LogDatabase.getDatabase(application).userDao()
    private val dietLogDao: DietLogDao = LogDatabase.getDatabase(application).dietLogDao()
    private val bloodGlucoseLogDao: BloodGlucoseLogDao =
        LogDatabase.getDatabase(application).bloodGlucoseLogDao()

    private val exerciseLogDao: ExerciseLogDao =
        LogDatabase.getDatabase(application).exerciseLogDao()

    private val medicationLogDao: MedicationLogDao =
        LogDatabase.getDatabase(application).medicationLogDao()

    // ----- User Operations-----
    fun getUserById(userId: String): Flow<UserEntity?> = userDao.getUser(userId)
    suspend fun insertUser(user: UserEntity) = userDao.insert(user)

    // ----- Diet Log Operations -----
    fun getAllDietLogs(): Flow<List<DietLogEntity>> = dietLogDao.getAllLogs()
    suspend fun insertDietLog(log: DietLogEntity) = dietLogDao.insert(log)

    // ----- Blood Glucose Log Operations -----
    fun getAllBloodGlucoseLogs(): Flow<List<BloodGlucoseLogEntity>> =
        bloodGlucoseLogDao.getAllLogs()

    suspend fun insertBloodGlucoseLog(log: BloodGlucoseLogEntity) = bloodGlucoseLogDao.insert(log)

    // ----- Exercise Log Operations -----
    fun getAllExerciseLogs(): Flow<List<ExerciseLogEntity>> = exerciseLogDao.getAllLogs()
    suspend fun insertExerciseLog(log: ExerciseLogEntity) = exerciseLogDao.insert(log)

    // ----- Medication Log Operations -----
    fun getAllMedicationLogs(): Flow<List<MedicationLogEntity>> = medicationLogDao.getAllLogs()
    suspend fun insertMedicationLog(log: MedicationLogEntity) = medicationLogDao.insert(log)


    // ----- Combine all logs into one feed for diary display -----
    fun getAllLogs(): Flow<List<LogDisplayItem>> {
        return combine(
            dietLogDao.getAllLogs(),
            bloodGlucoseLogDao.getAllLogs(),
            exerciseLogDao.getAllLogs(),
            medicationLogDao.getAllLogs()
        ) { dietLogs, glucoseLogs, exerciseLogs, medicationLogs ->
            val all = mutableListOf<LogDisplayItem>()
            all += dietLogs.map { it.toDisplayItem() }
            all += glucoseLogs.map { it.toDisplayItem() }
            all += exerciseLogs.map { it.toDisplayItem() }
            all += medicationLogs.map { it.toDisplayItem() }
            all.sortedByDescending { it.dateTime }
        }
    }
}
