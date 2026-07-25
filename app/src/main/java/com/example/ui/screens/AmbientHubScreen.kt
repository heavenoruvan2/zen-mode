package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AmbientSoundType
import com.example.ui.FocusFlowViewModel
import com.example.ui.components.CatState
import com.example.ui.components.EqualizerWaveAnimation
import com.example.ui.components.PixelCatWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbientHubScreen(
    viewModel: FocusFlowViewModel,
    onNavigateToFocus: () -> Unit = {},
    onToggleZen: () -> Unit = {}
) {
    val currentSound by viewModel.currentAmbientSound.collectAsState()
    val isPlaying = currentSound != AmbientSoundType.OFF

    var volume by remember { mutableStateOf(0.8f) }
    var selectedCategory by remember { mutableStateOf("All") }
    var sleepTimerMinutes by remember { mutableStateOf(0) }
    var simulatedProgress by remember { mutableStateOf(0.35f) }

    // Vinyl spinning animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val allSounds = remember {
        AmbientSoundType.values().filter { it != AmbientSoundType.OFF }
    }

    val categories = remember {
        listOf("All", "Nature", "Music", "Urban", "Cozy", "Focus")
    }

    val filteredSounds = remember(selectedCategory) {
        if (selectedCategory == "All") allSounds
        else allSounds.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    // Helper to get next/prev sound
    fun playNextSound() {
        val currentIndex = allSounds.indexOf(currentSound)
        val nextIndex = if (currentIndex < 0 || currentIndex == allSounds.lastIndex) 0 else currentIndex + 1
        viewModel.setAmbientSound(allSounds[nextIndex])
    }

    fun playPrevSound() {
        val currentIndex = allSounds.indexOf(currentSound)
        val prevIndex = if (currentIndex <= 0) allSounds.lastIndex else currentIndex - 1
        viewModel.setAmbientSound(allSounds[prevIndex])
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("ambient_hub_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "AMBIENT SOUNDSCAPES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Soundscape Hub 🎧",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onToggleZen() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🧘‍♂️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Zen View",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // MAIN AUDIO PLAYER CARD (Featured Soundscape)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("featured_audio_player_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Audio Album Art / Vinyl Disc Container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        Color(0xFF1E1B4B)
                                    )
                                )
                            )
                            .border(
                                width = 3.dp,
                                color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        // Inner spinning disc effect
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF111827))
                                .rotate(if (isPlaying) rotationAngle else 0f)
                        ) {
                            // Grooves on vinyl
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Black, CircleShape)
                                )
                            }
                        }

                        // Center Emoji Badge
                        Text(
                            text = if (isPlaying) currentSound.icon else "🎧",
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Track Title and Category
                    Text(
                        text = if (isPlaying) currentSound.displayName else "Select a Soundscape",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (isPlaying) currentSound.category else "Ambient Mode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (isPlaying) {
                            Spacer(modifier = Modifier.width(10.dp))
                            EqualizerWaveAnimation(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Audio Timeline / Playhead
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = simulatedProgress,
                            onValueChange = { simulatedProgress = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("10:45", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("∞ Loop", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("30:00", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Media Player Controls (Prev, Play/Pause, Next)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { playPrevSound() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Sound",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Large Play / Pause FAB
                        FloatingActionButton(
                            onClick = {
                                if (isPlaying) viewModel.setAmbientSound(AmbientSoundType.OFF)
                                else viewModel.setAmbientSound(AmbientSoundType.RAIN)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("ambient_play_pause_fab")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        IconButton(
                            onClick = { playNextSound() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Sound",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Volume Control & Sleep Timer Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Volume",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Slider(
                                value = volume,
                                onValueChange = { volume = it },
                                modifier = Modifier.fillMaxWidth(0.8f),
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary)
                            )
                        }

                        // Sleep Timer Quick Selection
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sleepTimerMinutes > 0) "${sleepTimerMinutes}m" else "Off",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    sleepTimerMinutes = when (sleepTimerMinutes) {
                                        0 -> 15
                                        15 -> 30
                                        30 -> 60
                                        else -> 0
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY FILTER BAR
        item {
            Column {
                Text(
                    text = "SOUNDSCAPE LIBRARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_cat_$cat")
                        )
                    }
                }
            }
        }

        // SOUNDSCAPE GRID / LIST
        items(filteredSounds) { sound ->
            val isSelected = currentSound == sound
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setAmbientSound(if (isSelected) AmbientSoundType.OFF else sound)
                    }
                    .testTag("soundscape_card_${sound.name}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(sound.icon, fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = sound.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${sound.category} • Continuous Relaxing Audio",
                                fontSize = 12.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    FilledIconButton(
                        onClick = {
                            viewModel.setAmbientSound(if (isSelected) AmbientSoundType.OFF else sound)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play sound",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // CAT COMPANION REACTION CARD
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelCatWidget(
                        catState = if (isPlaying) CatState.RESTING else CatState.PLAYFUL,
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Mochi's Sound Advice 🐾",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPlaying) "Mochi is drifting into deep focus with ${currentSound.displayName}! 💤"
                            else "Play Heavy Rain or Lofi Beats while studying to boost concentration!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // QUICK ACTION SHORTCUT
        item {
            Button(
                onClick = onNavigateToFocus,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("launch_focus_from_ambient_button")
            ) {
                Icon(imageVector = Icons.Default.Timer, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Focus Session with Soundscape", fontWeight = FontWeight.Bold)
            }
        }
    }
}
