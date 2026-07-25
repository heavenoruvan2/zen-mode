package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CatState
import com.example.ui.components.PixelCatWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameOnboardingDialog(
    initialName: String = "",
    onSaveName: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputName by remember(initialName) { mutableStateOf(initialName) }
    val presetNames = listOf("Alex", "Jordan", "Focus Master", "Zen Developer", "Sam")

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier.testTag("username_onboarding_dialog"),
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (initialName.isNotBlank()) "Edit Profile Name" else "Welcome to FocusFlow!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mochi wants to know what to call you!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PixelCatWidget(
                    catState = CatState.PLAYFUL,
                    overrideMessage = "Greeting Preview: Welcome back, ${inputName.ifBlank { "Alex" }}! 👋"
                )

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Your Name or Nickname") },
                    placeholder = { Text("e.g. Alex") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input_field"),
                    shape = RoundedCornerShape(16.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Quick Suggestions:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetNames) { name ->
                            SuggestionChip(
                                onClick = { inputName = name },
                                label = { Text(name, fontSize = 12.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = inputName.trim().ifBlank { "Focus Master" }
                    onSaveName(finalName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_username_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (initialName.isNotBlank()) "Save Name ✨" else "Get Started ✨", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (initialName.isNotBlank()) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_username_button")
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

