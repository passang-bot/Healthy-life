package com.example.data.repository

import com.example.data.local.FocusFitDao
import com.example.data.model.CompletionStatus
import com.example.data.model.DailyDebriefEntity
import com.example.data.model.DayDebriefSummary
import com.example.data.model.EnergyLevel
import com.example.data.model.ScheduleCategory
import com.example.data.model.ScheduleItemEntity
import com.example.data.model.SuggestedScheduleChange
import com.example.data.remote.GeminiDebriefAdvisor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FocusFitRepository(
    private val dao: FocusFitDao,
    private val advisor: GeminiDebriefAdvisor = GeminiDebriefAdvisor()
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, SuggestedScheduleChange::class.java)
    private val suggestionsAdapter = moshi.adapter<List<SuggestedScheduleChange>>(listType)

    fun getScheduleForDate(date: String): Flow<List<ScheduleItemEntity>> =
        dao.getScheduleItemsForDate(date)

    fun getDebriefForDate(date: String): Flow<DailyDebriefEntity?> =
        dao.getDebriefForDate(date)

    fun getAllDebriefs(): Flow<List<DailyDebriefEntity>> =
        dao.getAllDebriefs()

    fun getRecentItems(): Flow<List<ScheduleItemEntity>> =
        dao.getRecentScheduleItems()

    suspend fun addScheduleItem(item: ScheduleItemEntity): Long =
        dao.insertScheduleItem(item)

    suspend fun updateScheduleItem(item: ScheduleItemEntity) =
        dao.updateScheduleItem(item)

    suspend fun deleteScheduleItem(item: ScheduleItemEntity) =
        dao.deleteScheduleItem(item)

    suspend fun toggleStatus(item: ScheduleItemEntity, newStatus: CompletionStatus) {
        val isCompleted = newStatus == CompletionStatus.COMPLETED
        val updated = item.copy(
            completionStatus = newStatus.name,
            isCompleted = isCompleted
        )
        dao.updateScheduleItem(updated)
    }

    suspend fun updateFocusRating(item: ScheduleItemEntity, rating: Int) {
        dao.updateScheduleItem(item.copy(focusRating = rating))
    }

    suspend fun populateStarterScheduleIfEmpty(date: String) {
        val existing = dao.getScheduleItemsListForDate(date)
        if (existing.isEmpty()) {
            val starterItems = listOf(
                ScheduleItemEntity(
                    date = date,
                    title = "Morning Mobility & Cardio Run",
                    category = ScheduleCategory.FITNESS.name,
                    startTime = "07:00",
                    endTime = "07:45",
                    durationMinutes = 45,
                    isCompleted = false,
                    completionStatus = CompletionStatus.PENDING.name,
                    energyLevel = EnergyLevel.HIGH.name,
                    orderIndex = 1
                ),
                ScheduleItemEntity(
                    date = date,
                    title = "Deep Focus: High Impact Work",
                    category = ScheduleCategory.FOCUS.name,
                    startTime = "09:00",
                    endTime = "11:30",
                    durationMinutes = 150,
                    isCompleted = false,
                    completionStatus = CompletionStatus.PENDING.name,
                    energyLevel = EnergyLevel.HIGH.name,
                    orderIndex = 2
                ),
                ScheduleItemEntity(
                    date = date,
                    title = "Nutritious Protein Lunch & Walk",
                    category = ScheduleCategory.NUTRITION.name,
                    startTime = "12:00",
                    endTime = "13:00",
                    durationMinutes = 60,
                    isCompleted = false,
                    completionStatus = CompletionStatus.PENDING.name,
                    energyLevel = EnergyLevel.MEDIUM.name,
                    orderIndex = 3
                ),
                ScheduleItemEntity(
                    date = date,
                    title = "Afternoon Project Execution & Mails",
                    category = ScheduleCategory.FOCUS.name,
                    startTime = "14:00",
                    endTime = "16:30",
                    durationMinutes = 150,
                    isCompleted = false,
                    completionStatus = CompletionStatus.PENDING.name,
                    energyLevel = EnergyLevel.MEDIUM.name,
                    orderIndex = 4
                ),
                ScheduleItemEntity(
                    date = date,
                    title = "Strength Training / Gym Session",
                    category = ScheduleCategory.FITNESS.name,
                    startTime = "17:30",
                    endTime = "18:30",
                    durationMinutes = 60,
                    isCompleted = false,
                    completionStatus = CompletionStatus.PENDING.name,
                    energyLevel = EnergyLevel.HIGH.name,
                    orderIndex = 5
                ),
                ScheduleItemEntity(
                    date = date,
                    title = "Screen Sunset, Reading & Sleep Wind-down",
                    category = ScheduleCategory.RECOVERY.name,
                    startTime = "21:30",
                    endTime = "22:30",
                    durationMinutes = 60,
                    isCompleted = false,
                    completionStatus = CompletionStatus.PENDING.name,
                    energyLevel = EnergyLevel.LOW.name,
                    orderIndex = 6
                )
            )
            dao.insertScheduleItems(starterItems)
        }
    }

    suspend fun analyzeAndGenerateDebrief(
        date: String,
        waterGlasses: Int,
        energyLevel: String,
        reflections: String
    ): DailyDebriefEntity {
        val items = dao.getScheduleItemsListForDate(date)
        val completedCount = items.count { it.isCompleted }
        val totalCount = items.size
        val consistency = if (totalCount > 0) (completedCount * 100) / totalCount else 100

        val focusItems = items.filter { it.category == ScheduleCategory.FOCUS.name }
        val avgFocus = if (focusItems.isNotEmpty()) {
            (focusItems.map { it.focusRating }.average() * 2).toInt().coerceIn(1, 10)
        } else {
            7
        }

        val fitnessItems = items.filter { it.category == ScheduleCategory.FITNESS.name }
        val fitnessScore = if (fitnessItems.isNotEmpty()) {
            val fitnessCompleted = fitnessItems.count { it.isCompleted }
            ((fitnessCompleted.toDouble() / fitnessItems.size) * 10).toInt().coerceIn(1, 10)
        } else {
            6
        }

        val activeMinutes = fitnessItems.filter { it.isCompleted }.sumOf { it.durationMinutes }

        val summary: DayDebriefSummary = advisor.analyzeDayAndRecommend(
            date = date,
            items = items,
            waterGlasses = waterGlasses,
            energyLevel = energyLevel,
            reflectionNotes = reflections
        )

        val suggestionsJson = try {
            suggestionsAdapter.toJson(summary.suggestedChanges)
        } catch (e: Exception) {
            "[]"
        }

        val debriefEntity = DailyDebriefEntity(
            date = date,
            overallFocusScore = avgFocus,
            overallFitnessScore = fitnessScore,
            consistencyPercentage = consistency,
            completedBlocks = completedCount,
            totalBlocks = totalCount,
            waterGlasses = waterGlasses,
            activeMinutes = activeMinutes,
            energyLevel = energyLevel,
            reflectionNotes = reflections,
            aiFocusRecommendation = summary.focusAdvice,
            aiFitnessRecommendation = summary.fitnessAdvice,
            aiConsistencyRecommendation = summary.consistencyAdvice,
            tomorrowScheduleSuggestionsJson = suggestionsJson,
            appliedToTomorrow = false,
            timestamp = System.currentTimeMillis()
        )

        dao.insertOrUpdateDebrief(debriefEntity)
        return debriefEntity
    }

    suspend fun applySuggestionsToDate(targetDate: String, suggestionsJson: String): Boolean {
        return try {
            val suggestions = suggestionsAdapter.fromJson(suggestionsJson) ?: return false
            if (suggestions.isEmpty()) return false

            val newItems = suggestions.mapIndexed { index, s ->
                ScheduleItemEntity(
                    date = targetDate,
                    title = s.title,
                    category = s.category,
                    startTime = s.startTime,
                    endTime = s.endTime,
                    durationMinutes = s.durationMinutes,
                    isCompleted = false,
                    completionStatus = CompletionStatus.PENDING.name,
                    focusRating = 0,
                    energyLevel = EnergyLevel.HIGH.name,
                    notes = "Recommended by Coach: ${s.reason}",
                    orderIndex = index + 1
                )
            }

            // Remove existing uncompleted items or replace for targetDate
            dao.deleteScheduleItemsForDate(targetDate)
            dao.insertScheduleItems(newItems)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markDebriefApplied(date: String) {
        val existing = dao.getDebriefOnceForDate(date)
        if (existing != null) {
            dao.insertOrUpdateDebrief(existing.copy(appliedToTomorrow = true))
        }
    }

    fun parseSuggestions(json: String): List<SuggestedScheduleChange> {
        return try {
            suggestionsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
