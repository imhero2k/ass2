package com.example.fit5046_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        BloodGlucoseLogEntity::class,
        DietLogEntity::class,
        ExerciseLogEntity::class,
        MedicationLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LogDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bloodGlucoseLogDao(): BloodGlucoseLogDao
    abstract fun dietLogDao(): DietLogDao

    abstract fun exerciseLogDao(): ExerciseLogDao

    abstract fun medicationLogDao(): MedicationLogDao

    companion object {
        @Volatile
        private var INSTANCE: LogDatabase? = null

        fun getDatabase(context: Context): LogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LogDatabase::class.java,
                    "log_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}