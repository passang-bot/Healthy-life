package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyDebriefEntity
import com.example.data.model.ScheduleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusFitDao {

    @Query("SELECT * FROM schedule_items WHERE date = :date ORDER BY startTime ASC, id ASC")
    fun getScheduleItemsForDate(date: String): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items WHERE date = :date")
    suspend fun getScheduleItemsListForDate(date: String): List<ScheduleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItems(items: List<ScheduleItemEntity>)

    @Update
    suspend fun updateScheduleItem(item: ScheduleItemEntity)

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItemEntity)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleItemById(id: Long)

    @Query("DELETE FROM schedule_items WHERE date = :date")
    suspend fun deleteScheduleItemsForDate(date: String)

    @Query("SELECT * FROM daily_debriefs WHERE date = :date LIMIT 1")
    fun getDebriefForDate(date: String): Flow<DailyDebriefEntity?>

    @Query("SELECT * FROM daily_debriefs WHERE date = :date LIMIT 1")
    suspend fun getDebriefOnceForDate(date: String): DailyDebriefEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDebrief(debrief: DailyDebriefEntity)

    @Query("SELECT * FROM daily_debriefs ORDER BY date DESC")
    fun getAllDebriefs(): Flow<List<DailyDebriefEntity>>

    @Query("SELECT * FROM schedule_items ORDER BY date DESC, startTime ASC LIMIT 200")
    fun getRecentScheduleItems(): Flow<List<ScheduleItemEntity>>
}
