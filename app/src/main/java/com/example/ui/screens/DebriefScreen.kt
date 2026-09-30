package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DailyDebriefEntity
import com.example.data.model.EnergyLevel
import com.example.data.model.ScheduleCategory
import com.example.data.model.SuggestedScheduleChange
import com.example.ui.components.CategoryBadge
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.CategoryFitnessColor
import com.example.ui.theme.CategoryFocusColor
import com.example.ui.theme.CategoryNutritionColor
import com.example.ui.theme.CategoryRecoveryColor

@Composable
fun DebriefScreen(
    date: String,
    displayDate: String,
    debrief: DailyDebriefEntity?,
    waterGlasses: Int,
    energyLevel: String,
    reflectionNotes: String,
    isGenerating: Boolean,
    onWaterChange: (Int) -> Unit,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    onEnergyChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onGenerateDebrief: () -> Unit,
    onApplyToTomorrow: () -> Unit,
    parseSuggestions: (String) -> List<SuggestedScheduleChange>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 96.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Hero Header with Visual Banner
        item {
            HeroDebriefBanner(displayDate = displayDate)
        }

        // Daily Evening Check-in Form
        item {
            DailyCheckInCard(
                waterGlasses = waterGlasses,
                onIncrementWater = onIncrementWater,
                onDecrementWater = onDecrementWater,
                energyLevel = energyLevel,
                onEnergyChange = onEnergyChange,
                reflectionNotes = reflectionNotes,
                onNotesChange = onNotesChange
            )
        }

        // CTA Generate Button
        item {
            GenerateDebriefButton(
                isGenerating = isGenerating,
                hasExistingDebrief = debrief != null,
                onGenerate = onGenerateDebrief
            )
        }

        // AI Recommendations Result Section
        if (debrief != null) {
            item {
                RecommendationsResultCard(
                    debrief = debrief,
                    onApplyToTomorrow = onApplyToTomorrow,
                    parseSuggestions = parseSuggestions
                )
            }
        }
    }
}

@Composable
fun HeroDebriefBanner(displayDate: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.hero_daily_banner),
            contentDescription = "Daily Rhythm & Focus Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xEE0B111E)
                        ),
                        startY = 40f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "EVENING COACH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "End-of-Day Review & Calibration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Uncover tweaks for sharper focus, better fitness, and unbreakable consistency.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun DailyCheckInCard(
    waterGlasses: Int,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    energyLevel: String,
    onEnergyChange: (String) -> Unit,
    reflectionNotes: String,
    onNotesChange: (String) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Today's Physical & Mental Check-In",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Hydration Tracker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = "Water",
                        tint = CategoryRecoveryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Water Intake",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$waterGlasses / 8 glasses target",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrementWater,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_decrement_water")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Water")
                    }
                    Text(
                        text = "$waterGlasses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = onIncrementWater,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_increment_water")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Water")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Energy Level
            Text(
                text = "Overall Energy Level Today",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EnergyLevel.values().forEach { level ->
                    val isSelected = energyLevel == level.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEnergyChange(level.name) },
                        label = {
                            Text(
                                text = when (level) {
                                    EnergyLevel.HIGH -> "⚡ High"
                                    EnergyLevel.MEDIUM -> "🌿 Balanced"
                                    EnergyLevel.LOW -> "🔋 Low / Tired"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_energy_${level.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reflection Notes
            Text(
                text = "Daily Reflection & Distractions",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = reflectionNotes,
                onValueChange = onNotesChange,
                placeholder = {
                    Text("e.g. Afternoon focus was hard due to notifications; workout was great; felt energized after morning run.")
                },
                minLines = 3,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_reflection_notes")
            )
        }
    }
}

@Composable
fun GenerateDebriefButton(
    isGenerating: Boolean,
    hasExistingDebrief: Boolean,
    onGenerate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Button(
            onClick = onGenerate,
            enabled = !isGenerating,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_generate_ai_debrief")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Analyzing Schedule & Calibrating...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasExistingDebrief) "Re-Analyze & Update Recommendations" else "Analyze Day & Get AI Recommendations",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RecommendationsResultCard(
    debrief: DailyDebriefEntity,
    onApplyToTomorrow: () -> Unit,
    parseSuggestions: (String) -> List<SuggestedScheduleChange>
) {
    val suggestions = parseSuggestions(debrief.tomorrowScheduleSuggestionsJson)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Scores Bar
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ScoreColumn(
                    label = "Focus Score",
                    value = "${debrief.overallFocusScore}/10",
                    color = CategoryFocusColor
                )
                ScoreColumn(
                    label = "Fitness & Vitality",
                    value = "${debrief.overallFitnessScore}/10",
                    color = CategoryFitnessColor
                )
                ScoreColumn(
                    label = "Consistency",
                    value = "${debrief.consistencyPercentage}%",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Focus Recommendation Card
        RecommendationPillarCard(
            title = "Focus & Cognitive Optimization",
            icon = Icons.Default.Psychology,
            color = CategoryFocusColor,
            recommendation = debrief.aiFocusRecommendation,
            tag = "focus_rec"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fitness & Health Card
        RecommendationPillarCard(
            title = "Health, Movement & Recovery",
            icon = Icons.Default.FitnessCenter,
            color = CategoryFitnessColor,
            recommendation = debrief.aiFitnessRecommendation,
            tag = "fitness_rec"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Consistency & Habits Card
        RecommendationPillarCard(
            title = "Consistency & Schedule Systems",
            icon = Icons.Default.Repeat,
            color = MaterialTheme.colorScheme.primary,
            recommendation = debrief.aiConsistencyRecommendation,
            tag = "consistency_rec"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Tomorrow's Recommended Schedule Adjustments
        if (suggestions.isNotEmpty()) {
            Text(
                text = "Proposed Schedule Adjustments for Tomorrow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            suggestions.forEach { change ->
                SuggestedBlockItem(change = change)
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Apply Button
            Button(
                onClick = onApplyToTomorrow,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (debrief.appliedToTomorrow)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_apply_recommendations")
            ) {
                if (debrief.appliedToTomorrow) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Applied to Tomorrow's Schedule ✅", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Adjustments to Tomorrow's Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScoreColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecommendationPillarCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    recommendation: String,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SuggestedBlockItem(change: SuggestedScheduleChange) {
    val categoryColor = getCategoryColor(change.category)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = categoryColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${change.startTime}-${change.endTime}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = change.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = change.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
