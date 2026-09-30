package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyDebriefEntity
import com.example.data.model.ScheduleItemEntity

@Database(
    entities = [
        ScheduleItemEntity::class,
        DailyDebriefEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusFitDatabase : RoomDatabase() {
    abstract fun focusFitDao(): FocusFitDao

    companion object {
        @Volatile
        private var INSTANCE: FocusFitDatabase? = null

        fun getDatabase(context: Context): FocusFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusFitDatabase::class.java,
                    "focus_fit_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
