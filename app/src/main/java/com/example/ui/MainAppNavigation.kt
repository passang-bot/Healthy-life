package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddScheduleDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DebriefScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.viewmodel.FocusFitViewModel

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SCHEDULE("Daily Plan", Icons.Filled.Schedule, Icons.Outlined.Schedule, "tab_schedule"),
    DEBRIEF("Evening Coach", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_debrief"),
    ANALYTICS("Consistency", Icons.Filled.Insights, Icons.Outlined.Insights, "tab_analytics")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(viewModel: FocusFitViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.SCHEDULE) }
    var showAddDialog by remember { mutableStateOf(false) }

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val scheduleItems by viewModel.scheduleItems.collectAsStateWithLifecycle()
    val currentDebrief by viewModel.currentDebrief.collectAsStateWithLifecycle()
    val allDebriefs by viewModel.allDebriefs.collectAsStateWithLifecycle()
    val waterGlasses by viewModel.waterGlasses.collectAsStateWithLifecycle()
    val energyLevel by viewModel.energyLevel.collectAsStateWithLifecycle()
    val reflectionNotes by viewModel.reflectionNotes.collectAsStateWithLifecycle()
    val isGeneratingDebrief by viewModel.isGeneratingDebrief.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Handle back button for secondary tabs to return to primary SCHEDULE tab
    if (currentTab != AppTab.SCHEDULE) {
        BackHandler {
            currentTab = AppTab.SCHEDULE
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FocusFit",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    Text(
                        text = viewModel.formatDisplayDate(selectedDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                AppTab.SCHEDULE -> {
                    ScheduleScreen(
                        selectedDate = selectedDate,
                        items = scheduleItems,
                        onPreviousDay = { viewModel.goToPreviousDay() },
                        onNextDay = { viewModel.goToNextDay() },
                        onGoToToday = { viewModel.goToToday() },
                        formatDateDisplay = { viewModel.formatDisplayDate(it) },
                        onToggleStatus = { item, status -> viewModel.toggleItemStatus(item, status) },
                        onUpdateFocusRating = { item, rating -> viewModel.updateFocusRating(item, rating) },
                        onDeleteItem = { item -> viewModel.deleteItem(item) },
                        onPopulateStarter = { viewModel.populateStarterSchedule() },
                        onOpenAddDialog = { showAddDialog = true },
                        onNavigateToDebrief = { currentTab = AppTab.DEBRIEF }
                    )
                }
                AppTab.DEBRIEF -> {
                    DebriefScreen(
                        date = selectedDate,
                        displayDate = viewModel.formatDisplayDate(selectedDate),
                        debrief = currentDebrief,
                        waterGlasses = waterGlasses,
                        energyLevel = energyLevel,
                        reflectionNotes = reflectionNotes,
                        isGenerating = isGeneratingDebrief,
                        onWaterChange = { viewModel.setWaterGlasses(it) },
                        onIncrementWater = { viewModel.incrementWater() },
                        onDecrementWater = { viewModel.decrementWater() },
                        onEnergyChange = { viewModel.setEnergy(it) },
                        onNotesChange = { viewModel.setNotes(it) },
                        onGenerateDebrief = { viewModel.generateDebrief() },
                        onApplyToTomorrow = { viewModel.applyRecommendationsToTomorrow() },
                        parseSuggestions = { viewModel.getSuggestionsList(it) }
                    )
                }
                AppTab.ANALYTICS -> {
                    AnalyticsScreen(
                        debriefs = allDebriefs,
                        onSelectDate = { date ->
                            viewModel.selectDate(date)
                            currentTab = AppTab.SCHEDULE
                        },
                        formatDateDisplay = { viewModel.formatDisplayDate(it) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddScheduleDialog(
                onDismiss = { showAddDialog = false },
                onAddBlock = { title, category, start, end, duration, energy, notes ->
                    viewModel.addScheduleBlock(
                        title = title,
                        category = category,
                        startTime = start,
                        endTime = end,
                        durationMinutes = duration,
                        energyLevel = energy,
                        notes = notes
                    )
                }
            )
        }
    }
}
