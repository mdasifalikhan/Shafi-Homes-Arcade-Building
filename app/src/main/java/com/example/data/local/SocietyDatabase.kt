package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        MaintenanceDueEntity::class,
        ContractorEntity::class,
        RepairTicketEntity::class,
        ScheduleEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SocietyDatabase : RoomDatabase() {
    abstract fun societyDao(): SocietyDao

    companion object {
        @Volatile
        private var INSTANCE: SocietyDatabase? = null

        fun getInstance(context: Context): SocietyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SocietyDatabase::class.java,
                    "shafi_homes_arcade.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
