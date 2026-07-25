package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import com.example.ui.FocusFlowViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarViewMode {
    DAILY, WEEKLY, MONTHLY, AGENDA, TIMELINE, YEAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: FocusFlowViewModel,
    onOpenAddEvent: () -> Unit
) {
    val events by viewModel.allEvents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var currentViewMode by remember { mutableStateOf(CalendarViewMode.DAILY) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "Study", "College", "Work", "Exercise", "Meeting", "Shopping", "Health", "Finance", "Travel", "Personal")

    val filteredEvents = events.filter { event ->
        val matchesDate = when (currentViewMode) {
            CalendarViewMode.DAILY, CalendarViewMode.TIMELINE -> event.date == selectedDate
            else -> true
        }
        val matchesCat = if (selectedCategoryFilter == "All") true else event.category == selectedCategoryFilter
        matchesDate && matchesCat
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Header View Mode Switcher
        ScrollableTabRow(
            selectedTabIndex = currentViewMode.ordinal,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            CalendarViewMode.values().forEach { mode ->
                Tab(
                    selected = currentViewMode == mode,
                    onClick = { currentViewMode = mode },
                    text = {
                        Text(
                            text = mode.name.lowercase().capitalize(Locale.getDefault()),
                            fontWeight = if (currentViewMode == mode) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date Picker Bar (for Daily/Timeline)
        if (currentViewMode == CalendarViewMode.DAILY || currentViewMode == CalendarViewMode.TIMELINE) {
            DateSelectorRow(
                selectedDate = selectedDate,
                onSelectDate = { viewModel.setDate(it) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Category Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    modifier = Modifier.testTag("cat_chip_$cat")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Content List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            if (filteredEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📅", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No events for this selection", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Tap + below to add a new event or reminder", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    CalendarEventItem(
                        event = event,
                        onToggleComplete = { viewModel.toggleEventCompletion(event) },
                        onDelete = { viewModel.deleteEvent(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun DateSelectorRow(
    selectedDate: String,
    onSelectDate: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dates = remember {
        (0..14).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offset - 2)
            sdf.format(cal.time)
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(dates) { dateStr ->
            val dateObj = try { sdf.parse(dateStr) } catch (e: Exception) { Date() }
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(dateObj)
            val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(dateObj)
            val isSelected = dateStr == selectedDate

            Card(
                modifier = Modifier
                    .width(54.dp)
                    .height(68.dp)
                    .clickable { onSelectDate(dateStr) }
                    .testTag("date_card_$dateStr"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayName,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = dayNum,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarEventItem(
    event: EventEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = remember(event.colorHex) {
        try { Color(android.graphics.Color.parseColor(event.colorHex)) } catch (e: Exception) { Color(0xFF6366F1) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_event_${event.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = categoryColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = event.iconName, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (event.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${event.startTime} - ${event.endTime} • ${event.durationMinutes} mins",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = event.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                }
            }

            if (event.description.isNotBlank() || event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (event.description.isNotBlank()) {
                    Text(text = event.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                if (event.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = categoryColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = event.location, fontSize = 11.sp, color = categoryColor)
                    }
                }
            }
        }
    }
}
