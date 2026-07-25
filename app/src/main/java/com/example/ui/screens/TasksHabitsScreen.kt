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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import com.example.data.HabitEntity
import com.example.ui.FocusFlowViewModel

enum class SmartFolderTab {
    ALL, HIGH_PRIORITY, PINNED, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksHabitsScreen(
    viewModel: FocusFlowViewModel,
    onOpenAddEvent: () -> Unit
) {
    val events by viewModel.allEvents.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    var selectedTab by remember { mutableStateOf(SmartFolderTab.ALL) }

    val filteredEvents = events.filter { event ->
        when (selectedTab) {
            SmartFolderTab.ALL -> !event.isCompleted
            SmartFolderTab.HIGH_PRIORITY -> event.priority == "HIGH" && !event.isCompleted
            SmartFolderTab.PINNED -> event.isPinned
            SmartFolderTab.COMPLETED -> event.isCompleted
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Smart Folder Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                SmartFolderTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = when (tab) {
                                    SmartFolderTab.ALL -> "Pending Tasks"
                                    SmartFolderTab.HIGH_PRIORITY -> "🔥 High Priority"
                                    SmartFolderTab.PINNED -> "📌 Pinned"
                                    SmartFolderTab.COMPLETED -> "✅ Completed"
                                },
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        }

        // Habit Tracker Header & Cards
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habit Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(habits, key = { it.id }) { habit ->
                        HabitTrackerCard(
                            habit = habit,
                            onToggle = { viewModel.toggleHabitToday(habit) }
                        )
                    }
                }
            }
        }

        // Task List Section
        item {
            Text(
                text = "Tasks & Checklists",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (filteredEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No tasks in this folder", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
            }
        } else {
            items(filteredEvents, key = { it.id }) { event ->
                TaskCardItem(
                    event = event,
                    onToggleComplete = { viewModel.toggleEventCompletion(event) },
                    onDelete = { viewModel.deleteEvent(event) }
                )
            }
        }
    }
}

@Composable
fun HabitTrackerCard(
    habit: HabitEntity,
    onToggle: () -> Unit
) {
    val color = remember(habit.colorHex) {
        try { Color(android.graphics.Color.parseColor(habit.colorHex)) } catch (e: Exception) { Color(0xFF6366F1) }
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onToggle() }
            .testTag("habit_tracker_card_${habit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = habit.iconName, fontSize = 28.sp)
                IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (habit.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Complete Habit",
                        tint = if (habit.isCompletedToday) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = habit.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "🔥 ${habit.currentStreak} Days (Best: ${habit.bestStreak})", fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (habit.currentStreak % 7).toFloat() / 7f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun TaskCardItem(
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
            .testTag("task_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (event.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Complete",
                    tint = if (event.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (event.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${event.date} • ${event.startTime} • Priority: ${event.priority}",
                    fontSize = 11.sp,
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
    }
}
