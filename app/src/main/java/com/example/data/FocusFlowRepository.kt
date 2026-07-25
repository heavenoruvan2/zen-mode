package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FocusFlowRepository(private val db: FocusFlowDatabase) {
    val allEvents: Flow<List<EventEntity>> = db.eventDao().getAllEvents()
    val allHabits: Flow<List<HabitEntity>> = db.habitDao().getAllHabits()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = db.focusDao().getAllFocusSessions()
    val allBlockedApps: Flow<List<BlockedAppEntity>> = db.blockedAppDao().getAllBlockedApps()
    val totalFocusMinutes: Flow<Int?> = db.focusDao().getTotalFocusMinutes()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val currentEvents = db.eventDao().getAllEvents().first()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (currentEvents.isEmpty()) {
            val defaultEvents = listOf(
                EventEntity(
                    title = "Morning Deep Focus & Review",
                    description = "Planning day priorities & reviewing study notes",
                    date = todayStr,
                    startTime = "08:30",
                    endTime = "09:30",
                    durationMinutes = 60,
                    priority = "HIGH",
                    category = "Study",
                    colorHex = "#3182CE", // Blue
                    iconName = "📚",
                    notes = "Prepare notes for afternoon lectures",
                    location = "Library Quiet Zone",
                    reminderMinutesBefore = 15,
                    isCompleted = true,
                    isPinned = true
                ),
                EventEntity(
                    title = "Computer Science Algorithm Lab",
                    description = "Graph algorithms & Dynamic Programming practice",
                    date = todayStr,
                    startTime = "10:00",
                    endTime = "11:30",
                    durationMinutes = 90,
                    priority = "HIGH",
                    category = "College",
                    colorHex = "#38A169", // Green
                    iconName = "🏫",
                    notes = "Bring laptop & graph paper",
                    location = "Hall 4B",
                    reminderMinutesBefore = 10
                ),
                EventEntity(
                    title = "Kotlin & Compose Development",
                    description = "Build clean architecture UI state managers",
                    date = todayStr,
                    startTime = "13:00",
                    endTime = "15:00",
                    durationMinutes = 120,
                    priority = "HIGH",
                    category = "Work",
                    colorHex = "#805AD5", // Purple
                    iconName = "💻",
                    notes = "Refactor Room DAOs & Gemini API service",
                    location = "Home Office"
                ),
                EventEntity(
                    title = "Gym Cardio & Resistance Workout",
                    description = "Leg day session + 20 min HIIT cardio",
                    date = todayStr,
                    startTime = "16:00",
                    endTime = "17:15",
                    durationMinutes = 75,
                    priority = "MEDIUM",
                    category = "Exercise",
                    colorHex = "#DD6B20", // Orange
                    iconName = "🏋",
                    location = "Fitness First Gym"
                ),
                EventEntity(
                    title = "Team Sprint Planning Meeting",
                    description = "Sync with project leads on Q3 deliverables",
                    date = todayStr,
                    startTime = "18:00",
                    endTime = "18:45",
                    durationMinutes = 45,
                    priority = "MEDIUM",
                    category = "Meeting",
                    colorHex = "#00B5D8", // Cyan
                    iconName = "📞",
                    notes = "Share progress update slides"
                )
            )

            for (evt in defaultEvents) {
                db.eventDao().insertEvent(evt)
            }
        }

        val currentHabits = db.habitDao().getAllHabits().first()
        if (currentHabits.isEmpty()) {
            val defaultHabits = listOf(
                HabitEntity(title = "Read 20 pages", category = "Personal", currentStreak = 5, bestStreak = 14, colorHex = "#3182CE", iconName = "📖", isCompletedToday = true),
                HabitEntity(title = "Drink 3L Water", category = "Health", currentStreak = 8, bestStreak = 12, colorHex = "#E53E3E", iconName = "🚰", isCompletedToday = false),
                HabitEntity(title = "Coding Sprint 1hr", category = "Work", currentStreak = 11, bestStreak = 21, colorHex = "#805AD5", iconName = "💻", isCompletedToday = true),
                HabitEntity(title = "Meditation 10m", category = "Health", currentStreak = 3, bestStreak = 9, colorHex = "#DD6B20", iconName = "🧘", isCompletedToday = false)
            )
            for (h in defaultHabits) {
                db.habitDao().insertHabit(h)
            }
        }

        val currentBlockedApps = db.blockedAppDao().getAllBlockedApps().first()
        if (currentBlockedApps.isEmpty()) {
            val defaultBlocked = listOf(
                BlockedAppEntity("com.instagram.android", "Instagram", "📷", true, 4),
                BlockedAppEntity("com.facebook.katana", "Facebook", "👥", true, 2),
                BlockedAppEntity("com.google.android.youtube", "YouTube", "▶️", true, 7),
                BlockedAppEntity("com.zhiliaoapp.musically", "TikTok", "🎵", true, 9),
                BlockedAppEntity("com.twitter.android", "X (Twitter)", "🐦", true, 3),
                BlockedAppEntity("com.snapchat.android", "Snapchat", "👻", true, 1),
                BlockedAppEntity("com.discord", "Discord", "💬", true, 5)
            )
            db.blockedAppDao().insertBlockedApps(defaultBlocked)
        }
    }

    fun getEventsForDate(date: String): Flow<List<EventEntity>> = db.eventDao().getEventsForDate(date)

    suspend fun insertEvent(event: EventEntity) = db.eventDao().insertEvent(event)
    suspend fun updateEvent(event: EventEntity) = db.eventDao().updateEvent(event)
    suspend fun deleteEvent(event: EventEntity) = db.eventDao().deleteEvent(event)
    suspend fun setEventCompleted(id: Long, completed: Boolean) = db.eventDao().setCompleted(id, completed)

    suspend fun insertHabit(habit: HabitEntity) = db.habitDao().insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = db.habitDao().updateHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) = db.habitDao().deleteHabit(habit)

    suspend fun insertFocusSession(session: FocusSessionEntity) = db.focusDao().insertSession(session)
    suspend fun updateBlockedApp(app: BlockedAppEntity) = db.blockedAppDao().updateBlockedApp(app)
    suspend fun incrementBlockedAppAttempts(packageName: String) = db.blockedAppDao().incrementAttempts(packageName)
}
