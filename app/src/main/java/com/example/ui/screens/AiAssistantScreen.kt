package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FocusFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: FocusFlowViewModel
) {
    val aiAdvice by viewModel.aiScheduleAdvice.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    var nlText by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Natural Language Scheduler Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_nl_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Natural Language Scheduler",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Convert simple text into calendar events",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nlText,
                        onValueChange = { nlText = it },
                        placeholder = { Text("e.g. 'Tomorrow 3 PM Gym 1hr' or 'Friday 10 AM Math Exam'") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_nl_text_input"),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (nlText.isNotBlank()) {
                                viewModel.createEventFromNlPrompt(nlText) { created ->
                                    successMsg = "Created event: '${created.title}' on ${created.date} at ${created.startTime} (${created.category})"
                                    nlText = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_parse_button"),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isAiLoading && nlText.isNotBlank()
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Schedule")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Schedule with AI", fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = successMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = successMsg ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Smart Schedule Optimization Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_advice_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Routine Optimizer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { viewModel.generateAiAdvice() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Advice")
                        }
                    }

                    Text(
                        text = "Analyzes peak study times, break intervals & workload balance",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (aiAdvice == null) {
                        Button(
                            onClick = { viewModel.generateAiAdvice() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_advice_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Generate AI Schedule Insight")
                        }
                    } else {
                        Text(
                            text = aiAdvice ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
