package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FocusFlowViewModel
import com.example.ui.components.CatState
import com.example.ui.components.PixelCatWidget
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ZenModeScreen(
    viewModel: FocusFlowViewModel,
    onExitZen: () -> Unit
) {
    val events by viewModel.allEvents.collectAsState()
    val isFocusActive by viewModel.isFocusActive.collectAsState()
    val timerSecondsRemaining by viewModel.focusTimerSecondsRemaining.collectAsState()
    val focusTask by viewModel.focusTargetTask.collectAsState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val currentOrNextEvent = remember(events) {
        events.filter { it.date == todayStr && !it.isCompleted }.firstOrNull() ?: events.firstOrNull()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "zenBreathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(24.dp)
            .testTag("zen_mode_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Soft Breathing Aura
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(breathScale)
                .alpha(breathAlpha)
                .background(Color(0xFF1E293B), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Zen Mode",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ZEN MODE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 2.sp
                )
            }

            // Zen Pixel Cat
            PixelCatWidget(
                catState = CatState.ZEN,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Single Main Event / Task Display
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT SINGLE TASK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isFocusActive) focusTask else (currentOrNextEvent?.title ?: "No Pending Event"),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isFocusActive) {
                        val mins = timerSecondsRemaining / 60
                        val secs = timerSecondsRemaining % 60
                        Text(
                            text = String.format("%02d:%02d Remaining", mins, secs),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    } else if (currentOrNextEvent != null) {
                        Text(
                            text = "${currentOrNextEvent.startTime} - ${currentOrNextEvent.endTime} • ${currentOrNextEvent.category}",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        if (currentOrNextEvent.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentOrNextEvent.notes,
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ambient Sound Studio in Zen Mode
            com.example.ui.components.AmbienceStudioWidget(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onExitZen,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                modifier = Modifier.testTag("exit_zen_button")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exit Zen Mode", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
