package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class ScheduleCategory(val displayName: String, val emoji: String) {
    FOCUS("Focus & Work", "🎯"),
    FITNESS("Health & Fitness", "⚡"),
    NUTRITION("Meals & Fuel", "🥗"),
    RECOVERY("Rest & Wind-down", "🌙"),
    ROUTINE("Daily Routine", "📋")
}

enum class EnergyLevel {
    HIGH, MEDIUM, LOW
}

enum class CompletionStatus {
    PENDING,
    COMPLETED,
    PARTIAL,
    SKIPPED
}

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "YYYY-MM-DD"
    val title: String,
    val category: String, // from ScheduleCategory.name
    val startTime: String, // "09:00"
    val endTime: String, // "10:30"
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val completionStatus: String = CompletionStatus.PENDING.name,
    val focusRating: Int = 0, // 0 to 5
    val energyLevel: String = EnergyLevel.MEDIUM.name,
    val notes: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "daily_debriefs")
data class DailyDebriefEntity(
    @PrimaryKey
    val date: String, // "YYYY-MM-DD"
    val overallFocusScore: Int = 0, // 1-10
    val overallFitnessScore: Int = 0, // 1-10
    val consistencyPercentage: Int = 0, // 0-100
    val completedBlocks: Int = 0,
    val totalBlocks: Int = 0,
    val waterGlasses: Int = 0,
    val activeMinutes: Int = 0,
    val energyLevel: String = EnergyLevel.MEDIUM.name,
    val reflectionNotes: String = "",
    val aiFocusRecommendation: String = "",
    val aiFitnessRecommendation: String = "",
    val aiConsistencyRecommendation: String = "",
    val tomorrowScheduleSuggestionsJson: String = "",
    val appliedToTomorrow: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class SuggestedScheduleChange(
    val title: String,
    val category: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val reason: String
)

data class DayDebriefSummary(
    val focusAdvice: String,
    val fitnessAdvice: String,
    val consistencyAdvice: String,
    val suggestedChanges: List<SuggestedScheduleChange>
)
