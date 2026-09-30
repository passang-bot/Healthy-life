package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FocusFitDatabase
import com.example.data.model.CompletionStatus
import com.example.data.model.DailyDebriefEntity
import com.example.data.model.EnergyLevel
import com.example.data.model.ScheduleCategory
import com.example.data.model.ScheduleItemEntity
import com.example.data.model.SuggestedScheduleChange
import com.example.data.repository.FocusFitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FocusFitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FocusFitRepository

    init {
        val database = FocusFitDatabase.getDatabase(application)
        repository = FocusFitRepository(database.focusFitDao())
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)

    val todayDateString: String = dateFormat.format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val scheduleItems: StateFlow<List<ScheduleItemEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getScheduleForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentDebrief: StateFlow<DailyDebriefEntity?> = _selectedDate
        .flatMapLatest { date -> repository.getDebriefForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allDebriefs: StateFlow<List<DailyDebriefEntity>> = repository.getAllDebriefs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Debrief Input States
    private val _waterGlasses = MutableStateFlow(5)
    val waterGlasses: StateFlow<Int> = _waterGlasses.asStateFlow()

    private val _energyLevel = MutableStateFlow(EnergyLevel.MEDIUM.name)
    val energyLevel: StateFlow<String> = _energyLevel.asStateFlow()

    private val _reflectionNotes = MutableStateFlow("")
    val reflectionNotes: StateFlow<String> = _reflectionNotes.asStateFlow()

    private val _isGeneratingDebrief = MutableStateFlow(false)
    val isGeneratingDebrief: StateFlow<Boolean> = _isGeneratingDebrief.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Auto initialize starter schedule for today if empty
        viewModelScope.launch {
            repository.populateStarterScheduleIfEmpty(todayDateString)
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun goToPreviousDay() {
        _selectedDate.value = offsetDate(_selectedDate.value, -1)
    }

    fun goToNextDay() {
        _selectedDate.value = offsetDate(_selectedDate.value, 1)
    }

    fun goToToday() {
        _selectedDate.value = todayDateString
    }

    fun setWaterGlasses(count: Int) {
        _waterGlasses.value = count.coerceIn(0, 20)
    }

    fun incrementWater() {
        _waterGlasses.value = (_waterGlasses.value + 1).coerceAtMost(20)
    }

    fun decrementWater() {
        _waterGlasses.value = (_waterGlasses.value - 1).coerceAtLeast(0)
    }

    fun setEnergy(level: String) {
        _energyLevel.value = level
    }

    fun setNotes(notes: String) {
        _reflectionNotes.value = notes
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun toggleItemStatus(item: ScheduleItemEntity, newStatus: CompletionStatus) {
        viewModelScope.launch {
            repository.toggleStatus(item, newStatus)
        }
    }

    fun updateFocusRating(item: ScheduleItemEntity, rating: Int) {
        viewModelScope.launch {
            repository.updateFocusRating(item, rating)
        }
    }

    fun addScheduleBlock(
        title: String,
        category: ScheduleCategory,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        energyLevel: EnergyLevel,
        notes: String
    ) {
        viewModelScope.launch {
            val newItem = ScheduleItemEntity(
                date = _selectedDate.value,
                title = title,
                category = category.name,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                isCompleted = false,
                completionStatus = CompletionStatus.PENDING.name,
                focusRating = 0,
                energyLevel = energyLevel.name,
                notes = notes,
                orderIndex = (scheduleItems.value.maxOfOrNull { it.orderIndex } ?: 0) + 1
            )
            repository.addScheduleItem(newItem)
            _userMessage.value = "Scheduled block added!"
        }
    }

    fun deleteItem(item: ScheduleItemEntity) {
        viewModelScope.launch {
            repository.deleteScheduleItem(item)
            _userMessage.value = "Block removed"
        }
    }

    fun populateStarterSchedule() {
        viewModelScope.launch {
            repository.populateStarterScheduleIfEmpty(_selectedDate.value)
            _userMessage.value = "Loaded optimal balanced schedule!"
        }
    }

    fun generateDebrief() {
        val date = _selectedDate.value
        viewModelScope.launch {
            _isGeneratingDebrief.value = true
            try {
                repository.analyzeAndGenerateDebrief(
                    date = date,
                    waterGlasses = _waterGlasses.value,
                    energyLevel = _energyLevel.value,
                    reflections = _reflectionNotes.value
                )
                _userMessage.value = "Evening debrief & recommendations ready!"
            } catch (e: Exception) {
                _userMessage.value = "Could not finish debrief: ${e.message}"
            } finally {
                _isGeneratingDebrief.value = false
            }
        }
    }

    fun applyRecommendationsToTomorrow() {
        val debrief = currentDebrief.value ?: return
        val tomorrowDate = offsetDate(_selectedDate.value, 1)

        viewModelScope.launch {
            val success = repository.applySuggestionsToDate(
                targetDate = tomorrowDate,
                suggestionsJson = debrief.tomorrowScheduleSuggestionsJson
            )
            if (success) {
                repository.markDebriefApplied(debrief.date)
                _userMessage.value = "Applied recommended adjustments to tomorrow ($tomorrowDate)!"
            } else {
                _userMessage.value = "Failed to apply recommendations."
            }
        }
    }

    fun getSuggestionsList(json: String): List<SuggestedScheduleChange> {
        return repository.parseSuggestions(json)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val parsed = dateFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(parsed)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun offsetDate(dateStr: String, days: Int): String {
        return try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(dateStr) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, days)
            dateFormat.format(cal.time)
        } catch (e: Exception) {
            dateStr
        }
    }
}
