package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.BlockedAppEntity
import com.example.ui.FocusFlowViewModel

import com.example.service.AmbientSoundType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    viewModel: FocusFlowViewModel,
    onTriggerBlockOverlay: (appName: String) -> Unit
) {
    val isFocusActive by viewModel.isFocusActive.collectAsState()
    val timerSecondsRemaining by viewModel.focusTimerSecondsRemaining.collectAsState()
    val sessionMins by viewModel.focusSessionDurationMinutes.collectAsState()
    val focusTask by viewModel.focusTargetTask.collectAsState()
    val blockedApps by viewModel.allBlockedApps.collectAsState()
    val focusAttemptsCount by viewModel.focusAttemptsCount.collectAsState()
    val currentAmbientSound by viewModel.currentAmbientSound.collectAsState()
    val currentAccentHex by viewModel.currentAccentHex.collectAsState()

    var customMinsInput by remember { mutableStateOf("25") }
    var taskInput by remember { mutableStateOf("") }

    val mins = timerSecondsRemaining / 60
    val secs = timerSecondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", mins, secs)

    val progress = if (sessionMins > 0) {
        1f - (timerSecondsRemaining.toFloat() / (sessionMins * 60))
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Timer Dial Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_timer_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFocusActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isFocusActive) "🔥 DEEP FOCUS ACTIVE" else "🎯 FOCUS MODE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timer Circular View
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(170.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = focusTask,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isFocusActive) {
                        // Focus Target Input & Duration Presets
                        OutlinedTextField(
                            value = taskInput,
                            onValueChange = { taskInput = it },
                            placeholder = { Text("What are you focusing on?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("focus_task_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(15, 25, 45, 60).forEach { presetMins ->
                                FilterChip(
                                    selected = customMinsInput == presetMins.toString(),
                                    onClick = { customMinsInput = presetMins.toString() },
                                    label = { Text("${presetMins}m", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val minsToStart = customMinsInput.toIntOrNull() ?: 25
                                viewModel.startFocusSession(minsToStart, taskInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_focus_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Focus")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Focus Session", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Stop / Emergency Exit
                        Button(
                            onClick = { viewModel.stopFocusSessionEarly() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("stop_focus_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Focus Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Ambient Sound Generator Control Box
        item {
            com.example.ui.components.AmbienceStudioWidget(viewModel = viewModel)
        }

        // Accent Color Customizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎨 Custom Primary Accent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "#D0BCFF" to "Lavender",
                            "#381E72" to "Deep Violet",
                            "#10B981" to "Emerald",
                            "#3B82F6" to "Sapphire",
                            "#F43F5E" to "Rose Gold"
                        ).forEach { (hex, name) ->
                            val colorVal = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorVal)
                                    .clickable { viewModel.setAccentColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentAccentHex == hex) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Accessibility Service Consent & Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accessibility & Admin Service",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Monitors foreground apps to block distractions as soon as focus starts.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Test Simulation Trigger Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTriggerBlockOverlay("Instagram") }
                    .testTag("simulate_block_trigger"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE53E3E).copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Block, contentDescription = "Block Test", tint = Color(0xFFE53E3E))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulate App Open Attempt", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE53E3E))
                        Text("Opens Motivational Quote & Confirmation Screen", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = Color(0xFFE53E3E))
                }
            }
        }

        // Blocked Apps Manager Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blocked Social Apps",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Distraction Attempts: $focusAttemptsCount",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Blocked Apps List
        items(blockedApps, key = { it.packageName }) { app ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("blocked_app_${app.appName}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = app.iconEmoji, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = app.appName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = if (app.isBlocked) "Blocked during focus • ${app.attemptsCount} attempts blocked" else "Unblocked",
                            fontSize = 11.sp,
                            color = if (app.isBlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = app.isBlocked,
                        onCheckedChange = { viewModel.toggleBlockedApp(app) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
