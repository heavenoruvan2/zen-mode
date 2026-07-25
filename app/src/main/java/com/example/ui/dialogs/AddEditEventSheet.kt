package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventSheet(
    onDismiss: () -> Unit,
    onSave: (EventEntity) -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayStr) }
    var startTime by remember { mutableStateOf("14:00") }
    var endTime by remember { mutableStateOf("15:00") }
    var durationMinutes by remember { mutableStateOf(60) }
    var selectedCategory by remember { mutableStateOf("Study") }
    var selectedPriority by remember { mutableStateOf("HIGH") }
    var selectedIcons by remember { mutableStateOf(listOf("📚")) }
    var selectedColorHex by remember { mutableStateOf("#3182CE") }
    var reminderMins by remember { mutableStateOf(15) }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf(
        "Study" to "#3182CE",
        "College" to "#38A169",
        "Work" to "#805AD5",
        "Exercise" to "#DD6B20",
        "Meeting" to "#00B5D8",
        "Shopping" to "#D69E2E",
        "Health" to "#E53E3E",
        "Finance" to "#319795",
        "Travel" to "#D53F8C",
        "Personal" to "#718096"
    )

    val iconLibrary = listOf(
        "📚", "🏫", "💻", "🎮", "🏋", "🍔", "💊", "🛒", "✈", "🎵",
        "🎬", "❤️", "🧘", "💰", "📞", "📖", "✏", "🌙", "🚰", "🐶"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newEvt = EventEntity(
                            title = title,
                            description = description,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            durationMinutes = durationMinutes,
                            priority = selectedPriority,
                            category = selectedCategory,
                            colorHex = selectedColorHex,
                            iconName = selectedIcons.ifEmpty { listOf("📝") }.joinToString(" "),
                            reminderMinutesBefore = reminderMins,
                            location = location,
                            notes = notes
                        )
                        onSave(newEvt)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_event_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Event", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Schedule Event", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input"),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (yyyy-MM-dd)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(0.9f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End") },
                        modifier = Modifier.weight(0.9f),
                        singleLine = true
                    )
                }

                Text("Attach Icons / Badges (Tap to select multiple)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconLibrary) { icon ->
                        val isSelected = selectedIcons.contains(icon)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedIcons = if (isSelected) {
                                    if (selectedIcons.size > 1) selectedIcons - icon else selectedIcons
                                } else {
                                    if (selectedIcons.size < 4) selectedIcons + icon else selectedIcons
                                }
                            },
                            label = { Text(icon, fontSize = 18.sp) },
                            modifier = Modifier.testTag("icon_picker_$icon")
                        )
                    }
                }

                Text("Reminder Alert Before Event", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5 to "5m", 10 to "10m", 15 to "15m", 30 to "30m", 60 to "1h").forEach { (mins, label) ->
                        FilterChip(
                            selected = reminderMins == mins,
                            onClick = { reminderMins = mins },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text("Category & Color", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (cat, hex) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                selectedColorHex = hex
                            },
                            label = { Text(cat, fontSize = 12.sp) },
                            modifier = Modifier.testTag("cat_picker_$cat")
                        )
                    }
                }

                Text("Priority Level", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Checklist") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
