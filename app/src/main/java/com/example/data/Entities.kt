package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority {
    HIGH, MEDIUM, LOW
}

enum class RepeatOption {
    NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
}

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // yyyy-MM-dd
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val durationMinutes: Int = 60,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val category: String = "Personal", // Study, College, Work, Exercise, Meeting, Shopping, Health, Finance, Travel, Personal
    val colorHex: String = "#718096",
    val iconName: String = "📝",
    val notes: String = "",
    val location: String = "",
    val reminderMinutesBefore: Int = 15,
    val repeatOption: String = "NONE",
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val checklistItems: String = "", // JSON or pipe-separated string
    val tags: String = ""
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Personal",
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String = "", // yyyy-MM-dd
    val colorHex: String = "#4299E1",
    val iconName: String = "⭐",
    val isCompletedToday: Boolean = false
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 25,
    val completedMinutes: Int = 0,
    val blockedAppAttempts: Int = 0,
    val status: String = "COMPLETED", // COMPLETED, INTERRUPTED
    val targetTaskTitle: String = "Deep Focus"
)

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val iconEmoji: String,
    val isBlocked: Boolean = true,
    val attemptsCount: Int = 0
)
