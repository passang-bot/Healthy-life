package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EnergyLevel
import com.example.data.model.ScheduleCategory

data class PresetTemplate(
    val title: String,
    val category: ScheduleCategory,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val energy: EnergyLevel
)

@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onAddBlock: (
        title: String,
        category: ScheduleCategory,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        energyLevel: EnergyLevel,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ScheduleCategory.FOCUS) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var durationMinutes by remember { mutableStateOf(90) }
    var energyLevel by remember { mutableStateOf(EnergyLevel.HIGH) }
    var notes by remember { mutableStateOf("") }

    val presets = listOf(
        PresetTemplate("Deep Work Sprint", ScheduleCategory.FOCUS, "09:00", "11:30", 150, EnergyLevel.HIGH),
        PresetTemplate("HIIT & Core Workout", ScheduleCategory.FITNESS, "07:00", "07:45", 45, EnergyLevel.HIGH),
        PresetTemplate("Protein Fuel & Walk", ScheduleCategory.NUTRITION, "12:00", "13:00", 60, EnergyLevel.MEDIUM),
        PresetTemplate("Gym Resistance Training", ScheduleCategory.FITNESS, "17:30", "18:30", 60, EnergyLevel.HIGH),
        PresetTemplate("Evening Reading & Sleep Prep", ScheduleCategory.RECOVERY, "21:30", "22:30", 60, EnergyLevel.LOW),
        PresetTemplate("Quick Stretch & Hydration", ScheduleCategory.FITNESS, "15:00", "15:20", 20, EnergyLevel.MEDIUM)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add Schedule Block",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    presets.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = getCategoryColor(preset.category.name).copy(alpha = 0.15f),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    title = preset.title
                                    selectedCategory = preset.category
                                    startTime = preset.startTime
                                    endTime = preset.endTime
                                    durationMinutes = preset.duration
                                    energyLevel = preset.energy
                                }
                        ) {
                            Text(
                                text = "${getCategoryEmoji(preset.category.name)} ${preset.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = getCategoryColor(preset.category.name),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Block Title") },
                    placeholder = { Text("e.g. Deep Focus: Product Spec") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_block_title")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    ScheduleCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.emoji} ${cat.displayName}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = getCategoryColor(cat.name).copy(alpha = 0.25f),
                                selectedLabelColor = getCategoryColor(cat.name)
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_start_time")
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_end_time")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Expected Energy Level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnergyLevel.values().forEach { level ->
                        FilterChip(
                            selected = energyLevel == level,
                            onClick = { energyLevel = level },
                            label = { Text(level.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Objective") },
                    placeholder = { Text("e.g. Phone off, headphones on, goal 500 lines") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_block_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddBlock(
                            title.trim(),
                            selectedCategory,
                            startTime.trim(),
                            endTime.trim(),
                            durationMinutes,
                            energyLevel,
                            notes.trim()
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("btn_save_block")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
