package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiSchedulerService
import com.example.data.*
import com.example.service.FocusAccessibilityService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FocusFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FocusFlowRepository

    val allEvents: StateFlow<List<EventEntity>>
    val allHabits: StateFlow<List<HabitEntity>>
    val allFocusSessions: StateFlow<List<FocusSessionEntity>>
    val allBlockedApps: StateFlow<List<BlockedAppEntity>>
    val totalFocusMinutes: StateFlow<Int>

    // UI state
    val selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedCategoryFilter = MutableStateFlow("All")
    val selectedPriorityFilter = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")

    // Zen Mode State
    val isZenModeActive = MutableStateFlow(false)
    val activeReminderAlert = MutableStateFlow<String?>(null)

    // User Profile State
    val userName = MutableStateFlow("")
    val showUsernameDialog = MutableStateFlow(false)

    // Cat Companion Reaction State
    val catReactionMessage = MutableStateFlow<String?>(null)

    // Focus Mode Active Session State
    val isFocusActive = MutableStateFlow(false)
    val focusTimerSecondsRemaining = MutableStateFlow(25 * 60)
    val focusSessionDurationMinutes = MutableStateFlow(25)
    val focusTargetTask = MutableStateFlow("Deep Work Session")
    val focusAttemptsCount = MutableStateFlow(0)
    val showFocusBlockOverlay = MutableStateFlow(false)
    val currentBlockedAppAttemptName = MutableStateFlow("Instagram")

    // Theme & Ambience State
    val currentThemeMode = MutableStateFlow(com.example.ui.theme.ThemeMode.DARK)
    val currentAccentHex = MutableStateFlow("#D0BCFF")
    val currentAmbientSound = MutableStateFlow(com.example.service.AmbientSoundType.OFF)

    // AI state
    val aiScheduleAdvice = MutableStateFlow<String?>(null)
    val isAiLoading = MutableStateFlow(false)
    val nlInputText = MutableStateFlow("")

    private var timerJob: Job? = null

    fun saveUserName(name: String) {
        val clean = name.trim()
        userName.value = clean
        val prefs = getApplication<Application>().getSharedPreferences("FocusFlowPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("username", clean).apply()
        showUsernameDialog.value = false
        triggerCatReaction("Nice to meet you, $clean! Let's conquer the day!", com.example.ui.components.CatState.HAPPY)
    }

    fun openUsernameDialog() {
        showUsernameDialog.value = true
    }

    fun triggerCatReaction(msg: String, state: com.example.ui.components.CatState = com.example.ui.components.CatState.HAPPY) {
        catReactionMessage.value = msg
    }

    fun toggleZenMode() {
        val nextState = !isZenModeActive.value
        isZenModeActive.value = nextState
        if (nextState) {
            triggerCatReaction("Entering Zen Mode. Monochromatic, distraction-free view 🧘‍♂️", com.example.ui.components.CatState.ZEN)
        } else {
            triggerCatReaction("Exited Zen Mode. Welcome back! 🐾", com.example.ui.components.CatState.PLAYFUL)
        }
    }

    fun triggerTestReminder(eventTitle: String = "Important Meeting") {
        activeReminderAlert.value = "⏰ Event Reminder: '$eventTitle' is starting soon! (10m away)"
        triggerCatReaction("Alert! '$eventTitle' is starting in 10 minutes! ⏰", com.example.ui.components.CatState.ALERT)
    }

    fun dismissReminderAlert() {
        activeReminderAlert.value = null
    }

    fun setThemeMode(mode: com.example.ui.theme.ThemeMode) {
        currentThemeMode.value = mode
    }

    fun setAccentColor(hex: String) {
        currentAccentHex.value = hex
    }

    fun setAmbientSound(soundType: com.example.service.AmbientSoundType) {
        currentAmbientSound.value = soundType
        com.example.service.AmbientSoundPlayer.startSound(soundType)
    }

    init {
        val db = FocusFlowDatabase.getDatabase(application)
        repository = FocusFlowRepository(db)

        val prefs = application.getSharedPreferences("FocusFlowPrefs", android.content.Context.MODE_PRIVATE)
        val storedName = prefs.getString("username", "") ?: ""
        userName.value = storedName
        if (storedName.isBlank()) {
            showUsernameDialog.value = true
        }

        allEvents = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allHabits = repository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allFocusSessions = repository.allFocusSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allBlockedApps = repository.allBlockedApps.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        totalFocusMinutes = repository.totalFocusMinutes.map { it ?: 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        // Setup accessibility listener for blocked app attempt triggers
        FocusAccessibilityService.blockedAppAttemptListener = { pkg ->
            viewModelScope.launch {
                repository.incrementBlockedAppAttempts(pkg)
                focusAttemptsCount.value += 1
                val app = allBlockedApps.value.find { it.packageName == pkg }
                currentBlockedAppAttemptName.value = app?.appName ?: "Social Media App"
                showFocusBlockOverlay.value = true
                triggerCatReaction("Mochi caught you opening ${app?.appName ?: "a distractor app"}! Stay focused! 😼", com.example.ui.components.CatState.FOCUSING)
            }
        }
    }

    fun setDate(dateStr: String) {
        selectedDate.value = dateStr
    }

    fun toggleEventCompletion(event: EventEntity) {
        viewModelScope.launch {
            val nextStatus = !event.isCompleted
            repository.setEventCompleted(event.id, nextStatus)
            if (nextStatus) {
                triggerCatReaction("Awesome! Task '${event.title}' completed! 🎉", com.example.ui.components.CatState.HAPPY)
            }
        }
    }

    fun saveEvent(event: EventEntity) {
        viewModelScope.launch {
            if (event.id == 0L) {
                repository.insertEvent(event)
                triggerCatReaction("Task '${event.title}' scheduled! Mochi is ready! 🐾", com.example.ui.components.CatState.PLAYFUL)
            } else {
                repository.updateEvent(event)
            }
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun toggleHabitToday(habit: HabitEntity) {
        viewModelScope.launch {
            val isNowCompleted = !habit.isCompletedToday
            val newStreak = if (isNowCompleted) habit.currentStreak + 1 else (habit.currentStreak - 1).coerceAtLeast(0)
            val updated = habit.copy(
                isCompletedToday = isNowCompleted,
                currentStreak = newStreak,
                bestStreak = newStreak.coerceAtLeast(habit.bestStreak)
            )
            repository.updateHabit(updated)
            if (isNowCompleted) {
                triggerCatReaction("Habit streak level up! '${habit.title}' done! 🔥", com.example.ui.components.CatState.HAPPY)
            }
        }
    }

    fun toggleBlockedApp(app: BlockedAppEntity) {
        viewModelScope.launch {
            repository.updateBlockedApp(app.copy(isBlocked = !app.isBlocked))
        }
    }

    // Focus Mode Session Control
    fun startFocusSession(durationMins: Int, targetTask: String) {
        focusSessionDurationMinutes.value = durationMins
        focusTimerSecondsRemaining.value = durationMins * 60
        focusTargetTask.value = targetTask.ifBlank { "Deep Focus Session" }
        focusAttemptsCount.value = 0
        isFocusActive.value = true
        FocusAccessibilityService.isFocusModeActive = true
        FocusAccessibilityService.activeFocusTask = focusTargetTask.value
        triggerCatReaction("Focus mode ON! Mochi is in deep work mode! 💻", com.example.ui.components.CatState.FOCUSING)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isFocusActive.value && focusTimerSecondsRemaining.value > 0) {
                delay(1000)
                focusTimerSecondsRemaining.value -= 1
            }
            if (focusTimerSecondsRemaining.value <= 0 && isFocusActive.value) {
                completeFocusSession(status = "COMPLETED")
            }
        }
    }

    fun stopFocusSessionEarly() {
        viewModelScope.launch {
            completeFocusSession(status = "INTERRUPTED")
        }
    }

    private suspend fun completeFocusSession(status: String) {
        timerJob?.cancel()
        isFocusActive.value = false
        FocusAccessibilityService.isFocusModeActive = false
        val completedMins = ((focusSessionDurationMinutes.value * 60 - focusTimerSecondsRemaining.value) / 60).coerceAtLeast(1)

        val session = FocusSessionEntity(
            startTimeMillis = System.currentTimeMillis(),
            durationMinutes = focusSessionDurationMinutes.value,
            completedMinutes = completedMins,
            blockedAppAttempts = focusAttemptsCount.value,
            status = status,
            targetTaskTitle = focusTargetTask.value
        )
        repository.insertFocusSession(session)

        if (status == "COMPLETED") {
            triggerCatReaction("Glorious! You completed $completedMins min of deep focus! 🎉", com.example.ui.components.CatState.HAPPY)
        } else {
            triggerCatReaction("Session stopped. Take a break and try again! 💤", com.example.ui.components.CatState.RESTING)
        }
    }

    // AI Features
    fun createEventFromNlPrompt(prompt: String, onParsed: (EventEntity) -> Unit) {
        viewModelScope.launch {
            isAiLoading.value = true
            val parsed = GeminiSchedulerService.parseNaturalLanguageEvent(prompt)
            val newEvt = EventEntity(
                title = parsed.title,
                description = parsed.description,
                date = parsed.date.ifBlank { selectedDate.value },
                startTime = parsed.startTime,
                endTime = parsed.endTime,
                durationMinutes = parsed.durationMinutes,
                priority = parsed.priority,
                category = parsed.category,
                colorHex = parsed.colorHex,
                iconName = parsed.iconName
            )
            repository.insertEvent(newEvt)
            isAiLoading.value = false
            onParsed(newEvt)
        }
    }

    fun generateAiAdvice() {
        viewModelScope.launch {
            isAiLoading.value = true
            val advice = GeminiSchedulerService.getSmartScheduleAdvice(allEvents.value)
            aiScheduleAdvice.value = advice
            isAiLoading.value = false
        }
    }
}
