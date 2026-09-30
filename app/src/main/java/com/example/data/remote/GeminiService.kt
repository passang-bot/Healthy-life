package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DayDebriefSummary
import com.example.data.model.EnergyLevel
import com.example.data.model.ScheduleCategory
import com.example.data.model.ScheduleItemEntity
import com.example.data.model.SuggestedScheduleChange
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.4f,
    val topP: Float? = 0.9f
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiNetworkClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiDebriefAdvisor {

    suspend fun analyzeDayAndRecommend(
        date: String,
        items: List<ScheduleItemEntity>,
        waterGlasses: Int,
        energyLevel: String,
        reflectionNotes: String
    ): DayDebriefSummary = withContext(Dispatchers.IO) {
        val completedCount = items.count { it.isCompleted }
        val skippedCount = items.count { it.completionStatus == "SKIPPED" }
        val totalCount = items.size

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Try Gemini 3.5 Flash if API key is provided and not placeholder
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = buildPrompt(date, items, waterGlasses, energyLevel, reflectionNotes)
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.3f)
                )

                val response = GeminiNetworkClient.apiService.generateContent(apiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    val parsed = parseModelResponse(rawText)
                    if (parsed != null) {
                        return@withContext parsed
                    }
                }
            } catch (e: Exception) {
                Log.w("GeminiDebriefAdvisor", "Gemini API call failed or timed out, falling back to smart heuristic: ${e.message}")
            }
        }

        // High quality intelligent heuristic coach
        return@withContext generateSmartHeuristicDebrief(
            items = items,
            completedCount = completedCount,
            skippedCount = skippedCount,
            totalCount = totalCount,
            waterGlasses = waterGlasses,
            energyLevel = energyLevel,
            reflectionNotes = reflectionNotes
        )
    }

    private fun buildPrompt(
        date: String,
        items: List<ScheduleItemEntity>,
        waterGlasses: Int,
        energyLevel: String,
        reflectionNotes: String
    ): String {
        val scheduleSummary = items.joinToString("\n") {
            "- [${it.startTime} - ${it.endTime}] ${it.title} (${it.category}) -> Status: ${it.completionStatus}, Focus rating: ${it.focusRating}/5, Notes: ${it.notes.ifBlank { "None" }}"
        }

        return """
You are an elite productivity, fitness, and daily consistency coach.
Analyze the user's daily schedule log below and give concrete, actionable changes for tomorrow to optimize:
1. FOCUS: How to avoid distractions and protect peak energy hours.
2. HEALTH & FITNESS: Workout consistency, active movement, hydration, nutrition timing, and sleep recovery.
3. CONSISTENCY: Habit anchoring, sustainable scheduling, eliminating friction.

Daily Log for $date:
Water: $waterGlasses glasses
Reported Energy: $energyLevel
User Notes: ${reflectionNotes.ifBlank { "None provided" }}

Scheduled Blocks:
$scheduleSummary

Output your recommendations in this EXACT format (keep the section headers verbatim):
===FOCUS_RECOMMENDATION===
(Provide 2-3 specific, tactical, actionable sentences on what schedule or behavior changes will double focus tomorrow)

===FITNESS_RECOMMENDATION===
(Provide 2-3 specific, actionable sentences on fitness, movement, hydration, or recovery changes)

===CONSISTENCY_RECOMMENDATION===
(Provide 2-3 specific, actionable sentences on habit stacking and consistency systems)

===TOMORROW_SCHEDULE===
(Provide 4 to 6 scheduled blocks for tomorrow in format: START_TIME|END_TIME|TITLE|CATEGORY|REASON)
Example:
07:00|07:45|Morning Mobility & Cardio|FITNESS|Kickstart endorphins and prime daily focus
08:30|11:00|Deep Work Priority Sprint|FOCUS|Tackle hardest task during peak cognitive window
12:00|13:00|Healthy Lunch & Outdoor Walk|NUTRITION|Refuel with protein and get natural sunlight
14:00|16:00|Collaborative Tasks & Admin|FOCUS|Low-friction administrative work during afternoon dip
17:30|18:30|Gym Strength Session|FITNESS|Physical reset after workday
21:30|22:30|Digital Sunset & Reading|RECOVERY|Wind down nervous system for restorative sleep
""".trimIndent()
    }

    private fun parseModelResponse(text: String): DayDebriefSummary? {
        try {
            val focusHeader = "===FOCUS_RECOMMENDATION==="
            val fitnessHeader = "===FITNESS_RECOMMENDATION==="
            val consistencyHeader = "===CONSISTENCY_RECOMMENDATION==="
            val scheduleHeader = "===TOMORROW_SCHEDULE==="

            val focusIdx = text.indexOf(focusHeader)
            val fitnessIdx = text.indexOf(fitnessHeader)
            val consistencyIdx = text.indexOf(consistencyHeader)
            val scheduleIdx = text.indexOf(scheduleHeader)

            if (focusIdx == -1 || fitnessIdx == -1 || consistencyIdx == -1) {
                return null
            }

            val focus = text.substring(focusIdx + focusHeader.length, fitnessIdx).trim()
            val fitness = text.substring(fitnessIdx + fitnessHeader.length, consistencyIdx).trim()
            val consistency = if (scheduleIdx != -1) {
                text.substring(consistencyIdx + consistencyHeader.length, scheduleIdx).trim()
            } else {
                text.substring(consistencyIdx + consistencyHeader.length).trim()
            }

            val suggestedChanges = mutableListOf<SuggestedScheduleChange>()
            if (scheduleIdx != -1) {
                val scheduleText = text.substring(scheduleIdx + scheduleHeader.length).trim()
                scheduleText.lines().forEach { line ->
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size >= 5) {
                        val startTime = parts[0]
                        val endTime = parts[1]
                        val title = parts[2]
                        val category = when (parts[3].uppercase()) {
                            "FITNESS" -> ScheduleCategory.FITNESS.name
                            "FOCUS" -> ScheduleCategory.FOCUS.name
                            "NUTRITION" -> ScheduleCategory.NUTRITION.name
                            "RECOVERY" -> ScheduleCategory.RECOVERY.name
                            else -> ScheduleCategory.ROUTINE.name
                        }
                        val reason = parts[4]
                        suggestedChanges.add(
                            SuggestedScheduleChange(
                                title = title,
                                category = category,
                                startTime = startTime,
                                endTime = endTime,
                                durationMinutes = 60,
                                reason = reason
                            )
                        )
                    }
                }
            }

            return DayDebriefSummary(
                focusAdvice = focus,
                fitnessAdvice = fitness,
                consistencyAdvice = consistency,
                suggestedChanges = if (suggestedChanges.isNotEmpty()) suggestedChanges else getDefaultSuggestedSchedule()
            )
        } catch (e: Exception) {
            Log.e("GeminiDebriefAdvisor", "Failed parsing model response", e)
            return null
        }
    }

    private fun generateSmartHeuristicDebrief(
        items: List<ScheduleItemEntity>,
        completedCount: Int,
        skippedCount: Int,
        totalCount: Int,
        waterGlasses: Int,
        energyLevel: String,
        reflectionNotes: String
    ): DayDebriefSummary {
        val completionRate = if (totalCount > 0) (completedCount * 100) / totalCount else 70
        val hasMissedFitness = items.any { it.category == ScheduleCategory.FITNESS.name && !it.isCompleted }
        val focusBlocks = items.filter { it.category == ScheduleCategory.FOCUS.name }
        val avgFocusRating = if (focusBlocks.isNotEmpty()) {
            focusBlocks.map { it.focusRating }.average()
        } else 3.5

        val focusAdvice = buildString {
            if (avgFocusRating < 3.0 || items.any { it.notes.contains("distract", ignoreCase = true) }) {
                append("Protect your morning peak: Move your primary deep work block to 09:00 AM before checking messages. ")
                append("Implement 45-minute single-task sprints with your phone in do-not-disturb mode to eliminate micro-distractions.")
            } else if (completionRate < 60) {
                append("Your schedule had too much cognitive context-switching. Consolidate small tasks into one afternoon batch ")
                append("and schedule a dedicated 90-minute uninterrupted deep work block when your mental energy is fresh.")
            } else {
                append("Strong cognitive endurance today! To push higher tomorrow, pre-commit your top 2 non-negotiable deliverables ")
                append("the night before and schedule a 10-minute mental reset between intensive focus blocks.")
            }
        }

        val fitnessAdvice = buildString {
            if (hasMissedFitness) {
                append("Anchor your workout immediately after your morning wake-up routine or right at the end of the workday. ")
                append("When workouts are placed at the end of the evening, willpower fatigue leads to skips. A brisk 25-minute session counts!")
            } else {
                append("Great physical consistency! ")
            }
            if (waterGlasses < 6) {
                append("Your hydration was below target ($waterGlasses/8 glasses)—keep a 1L water bottle directly at your desk tomorrow. ")
            }
            if (energyLevel == EnergyLevel.LOW.name) {
                append("Low energy flagged: prioritize a 20-minute outdoor sunshine walk at 1:30 PM to suppress the post-lunch adenosine crash.")
            } else {
                append("Keep your movement routine consistent tomorrow with 30-45 minutes of moderate aerobic or resistance training.")
            }
        }

        val consistencyAdvice = buildString {
            if (skippedCount > 1 || completionRate < 65) {
                append("Focus on 'Micro-Wins': Reduce scheduled block count by 20% tomorrow to build buffer space for unexpected fires. ")
                append("A realistic schedule completed 100% builds 10x more momentum than an overfilled schedule completed 50%.")
            } else {
                append("Consistency is compounding nicely! Maintain the habit chain by shutting off digital screens 45 minutes before sleep ")
                append("and prepping your clothes and workspace the night before.")
            }
        }

        return DayDebriefSummary(
            focusAdvice = focusAdvice,
            fitnessAdvice = fitnessAdvice,
            consistencyAdvice = consistencyAdvice,
            suggestedChanges = getDefaultSuggestedSchedule()
        )
    }

    private fun getDefaultSuggestedSchedule(): List<SuggestedScheduleChange> {
        return listOf(
            SuggestedScheduleChange(
                title = "Hydrate & Morning Movement / Run",
                category = ScheduleCategory.FITNESS.name,
                startTime = "07:00",
                endTime = "07:45",
                durationMinutes = 45,
                reason = "Boosts morning cortisol naturally and prevents afternoon lethargy"
            ),
            SuggestedScheduleChange(
                title = "Deep Focus: Core Milestone Sprint",
                category = ScheduleCategory.FOCUS.name,
                startTime = "08:30",
                endTime = "11:00",
                durationMinutes = 150,
                reason = "Leverage morning peak alertness for your hardest cognitive task"
            ),
            SuggestedScheduleChange(
                title = "Nutritious Protein Lunch & Sunshine Walk",
                category = ScheduleCategory.NUTRITION.name,
                startTime = "12:00",
                endTime = "13:00",
                durationMinutes = 60,
                reason = "Reset circadian clock with daylight and steady blood sugar"
            ),
            SuggestedScheduleChange(
                title = "Secondary Focus: Tasks & Reviews",
                category = ScheduleCategory.FOCUS.name,
                startTime = "13:30",
                endTime = "15:30",
                durationMinutes = 120,
                reason = "Structured batching prevents context switching"
            ),
            SuggestedScheduleChange(
                title = "Gym Workout / HIIT / Yoga",
                category = ScheduleCategory.FITNESS.name,
                startTime = "17:30",
                endTime = "18:30",
                durationMinutes = 60,
                reason = "Physical decompression separating work from evening recovery"
            ),
            SuggestedScheduleChange(
                title = "Evening Wind-Down & Reading",
                category = ScheduleCategory.RECOVERY.name,
                startTime = "21:30",
                endTime = "22:30",
                durationMinutes = 60,
                reason = "Screen-free transition into deep restorative sleep"
            )
        )
    }
}
