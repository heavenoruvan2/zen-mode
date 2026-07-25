package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CatState(val statusMessage: String, val badgeIcon: String) {
    RESTING("Mochi is taking a catnap... 💤", "💤"),
    FOCUSING("Mochi is deep in deep work mode! 💻", "🔥"),
    HAPPY("Mochi is celebrating your task completion! 🎉", "🌟"),
    PLAYFUL("Mochi wants you to crush your next task! 🐾", "🐾"),
    ZEN("Mochi is meditating in Zen mode... 🧘‍♂️", "🧘‍♂️"),
    ALERT("Mochi detected an upcoming event reminder! ⏰", "⏰")
}

@Composable
fun PixelCatWidget(
    catState: CatState,
    modifier: Modifier = Modifier,
    overrideMessage: String? = null,
    onPetCat: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pixelCatAnim")
    val tailBlink by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailAnim"
    )

    var speechBubble by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(catState, overrideMessage) {
        speechBubble = overrideMessage ?: when (catState) {
            CatState.RESTING -> "Purrr... Ready to focus?"
            CatState.FOCUSING -> "You've got this! No distractions!"
            CatState.HAPPY -> "Awesome job finishing that!"
            CatState.PLAYFUL -> "Let's accomplish another task!"
            CatState.ZEN -> "Omm... Serene mind, single task focus."
            CatState.ALERT -> "Beep! Upcoming event starting soon!"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .clickable {
                speechBubble = listOf(
                    "Meow! Keep up the momentum!",
                    "Purr... Stay in the zone!",
                    "Focus time = Big brain energy! 🧠",
                    "I blocked all distractor apps for you! 🛡️"
                ).random()
                onPetCat()
            }
            .testTag("pixel_cat_widget"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pixel Canvas Cat
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF231F20), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        val pSize = size.width / 12f
                        val catOrange = Color(0xFFFF9800)
                        val catDarkOrange = Color(0xFFE65100)
                        val catWhite = Color(0xFFFFFFFF)
                        val catPink = Color(0xFFFF4081)
                        val catBlack = Color(0xFF111111)

                        fun drawPixel(x: Int, y: Int, color: Color) {
                            drawRect(
                                color = color,
                                topLeft = Offset(x * pSize, y * pSize),
                                size = Size(pSize, pSize)
                            )
                        }

                        // Ears
                        drawPixel(2, 1, catOrange)
                        drawPixel(3, 2, catPink)
                        drawPixel(8, 1, catOrange)
                        drawPixel(7, 2, catPink)

                        // Head Body
                        for (y in 2..8) {
                            for (x in 2..9) {
                                drawPixel(x, y, catOrange)
                            }
                        }

                        // Snout & Cheeks
                        drawPixel(4, 6, catWhite)
                        drawPixel(5, 6, catWhite)
                        drawPixel(6, 6, catWhite)
                        drawPixel(7, 6, catWhite)

                        // Nose
                        drawPixel(5, 5, catPink)
                        drawPixel(6, 5, catPink)

                        // Eyes based on state
                        if (catState == CatState.RESTING || catState == CatState.ZEN) {
                            // Sleeping/Meditating closed eyes - -
                            drawPixel(3, 4, catBlack)
                            drawPixel(4, 4, catBlack)
                            drawPixel(7, 4, catBlack)
                            drawPixel(8, 4, catBlack)
                        } else if (catState == CatState.FOCUSING) {
                            // Glasses / Focused Eyes
                            val glassColor = Color(0xFF80DEEA)
                            drawPixel(3, 3, glassColor)
                            drawPixel(4, 3, glassColor)
                            drawPixel(3, 4, glassColor)
                            drawPixel(4, 4, catBlack)
                            drawPixel(7, 3, glassColor)
                            drawPixel(8, 3, glassColor)
                            drawPixel(7, 4, glassColor)
                            drawPixel(8, 4, catBlack)
                        } else if (catState == CatState.ALERT) {
                            // Wide alert eyes
                            val alertRed = Color(0xFFFF5252)
                            drawPixel(3, 3, alertRed)
                            drawPixel(4, 3, alertRed)
                            drawPixel(3, 4, catWhite)
                            drawPixel(4, 4, catBlack)
                            drawPixel(7, 3, alertRed)
                            drawPixel(8, 3, alertRed)
                            drawPixel(7, 4, catWhite)
                            drawPixel(8, 4, catBlack)
                        } else {
                            // Bright cute open eyes
                            drawPixel(3, 4, catBlack)
                            drawPixel(4, 4, catWhite)
                            drawPixel(7, 4, catBlack)
                            drawPixel(8, 4, catWhite)
                        }

                        // Animated tail or paws
                        if (tailBlink > 0.5f) {
                            drawPixel(10, 7, catDarkOrange)
                            drawPixel(11, 6, catDarkOrange)
                        } else {
                            drawPixel(10, 8, catDarkOrange)
                            drawPixel(11, 7, catDarkOrange)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MOCHI THE PIXEL CAT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = catState.badgeIcon, fontSize = 12.sp)
                    }

                    Text(
                        text = speechBubble ?: catState.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Tap Mochi to pet 🐾",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
