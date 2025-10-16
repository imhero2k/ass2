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
import com.example.fit5046_app.data.SensorDataProvider
import com.example.fit5046_app.data.GlucoseSpikeAnalysisDao
import com.example.fit5046_app.data.GlucoseSpikeAnalysisEntity
import com.example.fit5046_app.data.GlucoseSpikeEntity
import com.example.fit5046_app.data.FoodGlucoseCorrelationEntity
import com.example.fit5046_app.data.FoodRecommendationEntity
import com.example.fit5046_app.data.CompleteAnalysisResult
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.LogDisplayItem
import com.example.fit5046_app.model.toDisplayItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime

class LogRepository(private val application: Application) {
    private val sensorDataProvider: SensorDataProvider by lazy {
        SensorDataProvider(application.applicationContext)
    }
    private val userDao: UserDao = LogDatabase.getDatabase(application).userDao()
    private val dietLogDao: DietLogDao = LogDatabase.getDatabase(application).dietLogDao()
    private val bloodGlucoseLogDao: BloodGlucoseLogDao =
        LogDatabase.getDatabase(application).bloodGlucoseLogDao()

    private val exerciseLogDao: ExerciseLogDao =
        LogDatabase.getDatabase(application).exerciseLogDao()

    private val medicationLogDao: MedicationLogDao =
        LogDatabase.getDatabase(application).medicationLogDao()
    
    private val glucoseSpikeAnalysisDao: GlucoseSpikeAnalysisDao =
        LogDatabase.getDatabase(application).glucoseSpikeAnalysisDao()

    // ----- User Operations-----
    fun getUserById(userId: String): Flow<UserEntity?> = userDao.getUser(userId)
    suspend fun insertUser(user: UserEntity) = userDao.insert(user)

    // ----- Diet Log Operations -----
    fun getAllDietLogs(): Flow<List<DietLogEntity>> = dietLogDao.getAllLogs()
    suspend fun getDietLogById(logId: Int): DietLogEntity? = dietLogDao.getLogById(logId)
    suspend fun insertDietLog(log: DietLogEntity) = dietLogDao.insert(log)
    suspend fun updateDietLog(log: DietLogEntity) = dietLogDao.update(log)
    suspend fun deleteDietLog(log: DietLogEntity) = dietLogDao.delete(log)
    suspend fun getDietLogsInTimeRange(userId: String, startTime: Long, endTime: Long): List<DietLogEntity> {
        return dietLogDao.getLogsInTimeRange(userId, startTime, endTime)
    }

    // ----- Blood Glucose Log Operations -----
    fun getAllBloodGlucoseLogs(): Flow<List<BloodGlucoseLogEntity>> =
        bloodGlucoseLogDao.getAllLogs()
    suspend fun getBloodGlucoseLogById(logId: Int): BloodGlucoseLogEntity? = bloodGlucoseLogDao.getLogById(logId)
    suspend fun insertBloodGlucoseLog(log: BloodGlucoseLogEntity) = bloodGlucoseLogDao.insert(log)
    suspend fun updateBloodGlucoseLog(log: BloodGlucoseLogEntity) = bloodGlucoseLogDao.update(log)
    suspend fun deleteBloodGlucoseLog(log: BloodGlucoseLogEntity) = bloodGlucoseLogDao.delete(log)
    suspend fun deleteSensorGeneratedGlucoseLogs(userId: String) {
        // Get all sensor-generated logs for this user
        val allLogs = bloodGlucoseLogDao.getAllLogs().first()
        val sensorLogs = allLogs.filter { 
            it.userId == userId && it.notes == "From sensor reading" 
        }
        // Delete each one
        sensorLogs.forEach { bloodGlucoseLogDao.delete(it) }
    }

    // ----- Exercise Log Operations -----
    fun getAllExerciseLogs(): Flow<List<ExerciseLogEntity>> = exerciseLogDao.getAllLogs()
    suspend fun getExerciseLogById(logId: Int): ExerciseLogEntity? = exerciseLogDao.getLogById(logId)
    suspend fun insertExerciseLog(log: ExerciseLogEntity) = exerciseLogDao.insert(log)
    suspend fun updateExerciseLog(log: ExerciseLogEntity) = exerciseLogDao.update(log)
    suspend fun deleteExerciseLog(log: ExerciseLogEntity) = exerciseLogDao.delete(log)
    suspend fun getExerciseLogsInTimeRange(userId: String, startTime: Long, endTime: Long): List<ExerciseLogEntity> {
        return exerciseLogDao.getLogsInTimeRange(userId, startTime, endTime)
    }

    // ----- Medication Log Operations -----
    fun getAllMedicationLogs(): Flow<List<MedicationLogEntity>> = medicationLogDao.getAllLogs()
    suspend fun getMedicationLogById(logId: Int): MedicationLogEntity? = medicationLogDao.getLogById(logId)
    suspend fun insertMedicationLog(log: MedicationLogEntity) = medicationLogDao.insert(log)
    suspend fun updateMedicationLog(log: MedicationLogEntity) = medicationLogDao.update(log)
    suspend fun deleteMedicationLog(log: MedicationLogEntity) = medicationLogDao.delete(log)
    suspend fun getMedicationLogsInTimeRange(userId: String, startTime: Long, endTime: Long): List<MedicationLogEntity> {
        return medicationLogDao.getLogsInTimeRange(userId, startTime, endTime)
    }


    // ----- Get all logs for diary display -----
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

    // ----- Get all logs for a specific user -----
    fun getAllLogsForUser(userId: String): Flow<List<LogDisplayItem>> {
        return combine(
            dietLogDao.getAllLogsForUser(userId),
            bloodGlucoseLogDao.getAllLogsForUser(userId),
            exerciseLogDao.getAllLogsForUser(userId),
            medicationLogDao.getAllLogsForUser(userId)
        ) { dietLogs, glucoseLogs, exerciseLogs, medicationLogs ->
            val all = mutableListOf<LogDisplayItem>()
            all += dietLogs.map { it.toDisplayItem() }
            all += glucoseLogs.map { it.toDisplayItem() }
            all += exerciseLogs.map { it.toDisplayItem() }
            all += medicationLogs.map { it.toDisplayItem() }
            all.sortedByDescending { it.dateTime }
        }
    }

    // ----- Sensor data operations -----

    // Flow of sensor readings that automatically saves to database
    // with contextual period determination
    fun getSensorDataFlow(): Flow<BloodGlucoseLogEntity> {
        return sensorDataProvider.sensorDataFlow().map { (currentTime, userId, glucoseValue) ->

            val period = determineContextualPeriod(userId, currentTime)
            
            val reading = BloodGlucoseLogEntity(
                userId = userId,
                dateTime = currentTime,
                period = period,
                notes = "From sensor reading",
                value = glucoseValue
            )
            insertBloodGlucoseLog(reading)
            reading
        }
    }

    // Determines contextual period for blood glucose by the most recent activity within 2 hours
    // By default, the period is determined by the current time of day
    private suspend fun determineContextualPeriod(userId: String, currentTime: LocalDateTime): EntryPeriod {
        // Time range of the last 2 hours
        val twoHoursAgoMillis = currentTime.minusHours(2).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentTimeMillis = currentTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // Get the most recent logs of each type within the last 2 hours
        val dietLogs = getDietLogsInTimeRange(userId, twoHoursAgoMillis, currentTimeMillis)
        val exerciseLogs = getExerciseLogsInTimeRange(userId, twoHoursAgoMillis, currentTimeMillis)
        val medicationLogs = getMedicationLogsInTimeRange(userId, twoHoursAgoMillis, currentTimeMillis)
        // logs are sorted DESC by query
        val recentDiet = dietLogs.firstOrNull() 
        val recentExercise = exerciseLogs.firstOrNull()
        val recentMedication = medicationLogs.firstOrNull()
        val allRecent = listOfNotNull(
            recentDiet?.let { it to it.dateTime },
            recentExercise?.let { it to it.dateTime },
            recentMedication?.let { it to it.dateTime }
        ).sortedByDescending { it.second }
        
        return when (val mostRecent = allRecent.firstOrNull()?.first) {
            is DietLogEntity -> {
                when (mostRecent.period) {
                    EntryPeriod.BREAKFAST -> EntryPeriod.AFTER_BREAKFAST
                    EntryPeriod.LUNCH -> EntryPeriod.AFTER_LUNCH
                    EntryPeriod.DINNER -> EntryPeriod.AFTER_DINNER
                    EntryPeriod.SNACK -> EntryPeriod.AFTER_SNACK
                    else -> getPeriodFromTime(currentTime.toLocalTime())
                }
            }
            is ExerciseLogEntity -> EntryPeriod.AFTER_EXERCISE
            is MedicationLogEntity -> EntryPeriod.AFTER_MEDICATION
            else -> getPeriodFromTime(currentTime.toLocalTime())
        }
    }

    // Determine the period based on time of day if no preceding activity is found
    private fun getPeriodFromTime(time: LocalTime): EntryPeriod {
        return when (time.hour) {
            in 5..11 -> EntryPeriod.MORNING
            in 12..16 -> EntryPeriod.AFTERNOON
            in 17..20 -> EntryPeriod.EVENING
            else -> EntryPeriod.NIGHT
        }
    }
    
    // ----- Glucose Spike Analysis Operations -----
    
    // Get all analyses for a user
    fun getAllAnalysesForUser(userId: String): Flow<List<GlucoseSpikeAnalysisEntity>> =
        glucoseSpikeAnalysisDao.getAllAnalysesForUser(userId)
    
    // Get the latest analysis for a user
    fun getLatestAnalysisForUser(userId: String): Flow<GlucoseSpikeAnalysisEntity?> =
        glucoseSpikeAnalysisDao.getLatestAnalysisForUser(userId)
    
    // Get complete analysis with all related data
    suspend fun getCompleteAnalysis(analysisId: Int): CompleteAnalysisResult? =
        glucoseSpikeAnalysisDao.getCompleteAnalysis(analysisId)
    
    suspend fun getLatestCompleteAnalysis(userId: String): CompleteAnalysisResult? =
        glucoseSpikeAnalysisDao.getLatestCompleteAnalysis(userId)
    
    // Get spikes for a specific analysis
    fun getSpikesForAnalysis(analysisId: Int): Flow<List<GlucoseSpikeEntity>> =
        glucoseSpikeAnalysisDao.getSpikesForAnalysis(analysisId)
    
    // Get all spikes for a user
    fun getAllSpikesForUser(userId: String): Flow<List<GlucoseSpikeEntity>> =
        glucoseSpikeAnalysisDao.getAllSpikesForUser(userId)
    
    // Get spikes in date range
    fun getSpikesInDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<GlucoseSpikeEntity>> =
        glucoseSpikeAnalysisDao.getSpikesInDateRange(userId, startDate, endDate)
    
    // Get correlations for a specific analysis
    fun getCorrelationsForAnalysis(analysisId: Int): Flow<List<FoodGlucoseCorrelationEntity>> =
        glucoseSpikeAnalysisDao.getCorrelationsForAnalysis(analysisId)
    
    // Get all correlations for a user
    fun getAllCorrelationsForUser(userId: String): Flow<List<FoodGlucoseCorrelationEntity>> =
        glucoseSpikeAnalysisDao.getAllCorrelationsForUser(userId)
    
    // Get recommendations for a specific analysis
    fun getRecommendationsForAnalysis(analysisId: Int): Flow<List<FoodRecommendationEntity>> =
        glucoseSpikeAnalysisDao.getRecommendationsForAnalysis(analysisId)
    
    // Get all recommendations for a user
    fun getAllRecommendationsForUser(userId: String): Flow<List<FoodRecommendationEntity>> =
        glucoseSpikeAnalysisDao.getAllRecommendationsForUser(userId)
    
    // Get recommendations by type
    fun getRecommendationsByType(userId: String, recommendationType: String): Flow<List<FoodRecommendationEntity>> =
        glucoseSpikeAnalysisDao.getRecommendationsByType(userId, recommendationType)
    
    // Save complete analysis results
    suspend fun saveAnalysisResults(
        analysis: GlucoseSpikeAnalysisEntity,
        spikes: List<GlucoseSpikeEntity>,
        correlations: List<FoodGlucoseCorrelationEntity>,
        recommendations: List<FoodRecommendationEntity>
    ) {
        val analysisId = glucoseSpikeAnalysisDao.insertAnalysis(analysis).toInt()
        
        // Update spikes with analysis ID
        val spikesWithAnalysisId = spikes.map { it.copy(analysisId = analysisId) }
        glucoseSpikeAnalysisDao.insertSpikes(spikesWithAnalysisId)
        
        // Update correlations with analysis ID
        val correlationsWithAnalysisId = correlations.map { it.copy(analysisId = analysisId) }
        glucoseSpikeAnalysisDao.insertCorrelations(correlationsWithAnalysisId)
        
        // Update recommendations with analysis ID
        val recommendationsWithAnalysisId = recommendations.map { it.copy(analysisId = analysisId) }
        glucoseSpikeAnalysisDao.insertRecommendations(recommendationsWithAnalysisId)
    }
    
    // Delete analysis and all related data
    suspend fun deleteAnalysis(analysis: GlucoseSpikeAnalysisEntity) {
        glucoseSpikeAnalysisDao.deleteSpikesForAnalysis(analysis.id)
        glucoseSpikeAnalysisDao.deleteCorrelationsForAnalysis(analysis.id)
        glucoseSpikeAnalysisDao.deleteRecommendationsForAnalysis(analysis.id)
        glucoseSpikeAnalysisDao.deleteAnalysis(analysis)
    }
    
    // Delete all analyses for a user
    suspend fun deleteAllAnalysesForUser(userId: String) {
        glucoseSpikeAnalysisDao.deleteAllAnalysesForUser(userId)
    }
}