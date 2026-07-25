package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.dialogs.AddEditEventSheet
import com.example.ui.dialogs.FocusBlockOverlayDialog
import com.example.ui.dialogs.UsernameOnboardingDialog
import com.example.ui.screens.*

enum class FocusFlowNavTab {
    DASHBOARD, CALENDAR, FOCUS_MODE, AMBIENT_HUB, TASKS_HABITS, AI_ASSISTANT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusFlowApp(
    viewModel: FocusFlowViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(FocusFlowNavTab.DASHBOARD) }
    var showAddEventSheet by remember { mutableStateOf(false) }
    var showNlQuickAddSheet by remember { mutableStateOf(false) }
    var nlPromptInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }

    val showOverlay by viewModel.showFocusBlockOverlay.collectAsState()
    val blockedAppName by viewModel.currentBlockedAppAttemptName.collectAsState()
    val focusTask by viewModel.focusTargetTask.collectAsState()
    val focusTimerSecs by viewModel.focusTimerSecondsRemaining.collectAsState()
    val focusAttempts by viewModel.focusAttemptsCount.collectAsState()

    val isZenModeActive by viewModel.isZenModeActive.collectAsState()
    val activeReminderAlert by viewModel.activeReminderAlert.collectAsState()

    val currentThemeMode by viewModel.currentThemeMode.collectAsState()
    val currentAccentHex by viewModel.currentAccentHex.collectAsState()

    val parsedAccentColor = remember(currentAccentHex) {
        try {
            Color(android.graphics.Color.parseColor(currentAccentHex))
        } catch (e: Exception) {
            Color(0xFFD0BCFF)
        }
    }

    com.example.ui.theme.FocusFlowTheme(
        themeMode = currentThemeMode,
        customAccentColor = parsedAccentColor
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.focus_logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "FocusFlow",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.openUsernameDialog() },
                            modifier = Modifier.testTag("top_bar_profile_button")
                        ) {
                            Text("👤", fontSize = 20.sp)
                        }
                        IconButton(
                            onClick = { activeTab = FocusFlowNavTab.AMBIENT_HUB },
                            modifier = Modifier.testTag("ambient_hub_top_button")
                        ) {
                            Text("🎧", fontSize = 20.sp)
                        }
                        IconButton(
                            onClick = { viewModel.toggleZenMode() },
                            modifier = Modifier.testTag("zen_mode_toggle_button")
                        ) {
                            Text(if (isZenModeActive) "🧘‍♂️" else "🧘", fontSize = 20.sp)
                        }
                        IconButton(onClick = { showNlQuickAddSheet = true }) {
                            Text("✨", fontSize = 20.sp)
                        }
                        IconButton(onClick = { showSearchField = !showSearchField }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    FocusFlowNavTab.values().forEach { tab ->
                        val isSelected = activeTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        FocusFlowNavTab.DASHBOARD -> Icons.Default.Dashboard
                                        FocusFlowNavTab.CALENDAR -> Icons.Default.CalendarToday
                                        FocusFlowNavTab.FOCUS_MODE -> Icons.Default.Timer
                                        FocusFlowNavTab.AMBIENT_HUB -> Icons.Default.GraphicEq
                                        FocusFlowNavTab.TASKS_HABITS -> Icons.Default.FormatListNumbered
                                        FocusFlowNavTab.AI_ASSISTANT -> Icons.Default.AutoAwesome
                                    },
                                    contentDescription = tab.name
                                )
                            },
                            label = {
                                Text(
                                    text = when (tab) {
                                        FocusFlowNavTab.DASHBOARD -> "Home"
                                        FocusFlowNavTab.CALENDAR -> "Calendar"
                                        FocusFlowNavTab.FOCUS_MODE -> "Focus"
                                        FocusFlowNavTab.AMBIENT_HUB -> "Ambience"
                                        FocusFlowNavTab.TASKS_HABITS -> "Tasks"
                                        FocusFlowNavTab.AI_ASSISTANT -> "AI Coach"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddEventSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_event_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(visible = activeReminderAlert != null) {
                        Surface(
                            color = Color(0xFF6366F1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reminder_alert_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("⏰", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = activeReminderAlert ?: "",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                TextButton(onClick = { viewModel.dismissReminderAlert() }) {
                                    Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showSearchField) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search events, tags, categories...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("global_search_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    showSearchField = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            if (targetState.ordinal > initialState.ordinal) {
                                (slideInHorizontally(animationSpec = tween(280)) { width -> width / 3 } + fadeIn(animationSpec = tween(280))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(280)) { width -> -width / 3 } + fadeOut(animationSpec = tween(280)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(280)) { width -> -width / 3 } + fadeIn(animationSpec = tween(280))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(280)) { width -> width / 3 } + fadeOut(animationSpec = tween(280)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "tab_transition"
                    ) { targetTab ->
                        when (targetTab) {
                            FocusFlowNavTab.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToFocus = { activeTab = FocusFlowNavTab.FOCUS_MODE },
                                onNavigateToCalendar = { activeTab = FocusFlowNavTab.CALENDAR },
                                onOpenAddEvent = { showAddEventSheet = true },
                                onOpenNlSheet = { showNlQuickAddSheet = true }
                            )
                            FocusFlowNavTab.CALENDAR -> CalendarScreen(
                                viewModel = viewModel,
                                onOpenAddEvent = { showAddEventSheet = true }
                            )
                            FocusFlowNavTab.FOCUS_MODE -> FocusModeScreen(
                                viewModel = viewModel,
                                onTriggerBlockOverlay = { appName ->
                                    viewModel.currentBlockedAppAttemptName.value = appName
                                    viewModel.showFocusBlockOverlay.value = true
                                }
                            )
                            FocusFlowNavTab.AMBIENT_HUB -> AmbientHubScreen(
                                viewModel = viewModel,
                                onNavigateToFocus = { activeTab = FocusFlowNavTab.FOCUS_MODE },
                                onToggleZen = { viewModel.toggleZenMode() }
                            )
                            FocusFlowNavTab.TASKS_HABITS -> TasksHabitsScreen(
                                viewModel = viewModel,
                                onOpenAddEvent = { showAddEventSheet = true }
                            )
                            FocusFlowNavTab.AI_ASSISTANT -> AiAssistantScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    if (isZenModeActive) {
        com.example.ui.screens.ZenModeScreen(
            viewModel = viewModel,
            onExitZen = { viewModel.toggleZenMode() }
        )
    }

    // Add / Edit Event Sheet
    if (showAddEventSheet) {
        AddEditEventSheet(
            onDismiss = { showAddEventSheet = false },
            onSave = { event -> viewModel.saveEvent(event) }
        )
    }

    // Natural Language Quick Add Sheet
    if (showNlQuickAddSheet) {
        AlertDialog(
            onDismissRequest = { showNlQuickAddSheet = false },
            title = { Text("✨ AI Natural Language Quick Add", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Type what you want to schedule:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nlPromptInput,
                        onValueChange = { nlPromptInput = it },
                        placeholder = { Text("e.g. 'Tomorrow 3 PM Gym for 1 hour'") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nl_quick_add_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nlPromptInput.isNotBlank()) {
                            viewModel.createEventFromNlPrompt(nlPromptInput) {}
                            nlPromptInput = ""
                            showNlQuickAddSheet = false
                        }
                    },
                    modifier = Modifier.testTag("nl_quick_add_submit")
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNlQuickAddSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Focus Mode Block Overlay Dialog
    if (showOverlay) {
        FocusBlockOverlayDialog(
            blockedAppName = blockedAppName,
            currentTaskTitle = focusTask,
            remainingSeconds = focusTimerSecs,
            attemptsCount = focusAttempts,
            onDismiss = { viewModel.showFocusBlockOverlay.value = false },
            onEmergencyQuit = {
                viewModel.stopFocusSessionEarly()
                viewModel.showFocusBlockOverlay.value = false
            }
        )
    }

    // Username Onboarding Dialog
    val showUsernameDialog by viewModel.showUsernameDialog.collectAsState()
    val userName by viewModel.userName.collectAsState()
    if (showUsernameDialog) {
        UsernameOnboardingDialog(
            initialName = userName,
            onSaveName = { name -> viewModel.saveUserName(name) },
            onDismiss = { viewModel.showUsernameDialog.value = false }
        )
    }
}
