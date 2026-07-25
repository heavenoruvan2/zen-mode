package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    DARK, LIGHT, AMOLED, MATERIAL_YOU
}

private val BaseDarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    secondary = VioletSecondary,
    tertiary = CyanAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color(0xFF381E72),
    onSecondary = Color(0xFFD0BCFF),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val BaseLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFFEF7FF),
    surfaceVariant = Color(0xFFE7E0EC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
    onSurfaceVariant = Color(0xFF49454F)
)

private val AmoledColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    secondary = VioletSecondary,
    tertiary = CyanAccent,
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF381E72),
    onSecondary = Color(0xFFD0BCFF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFCCCCCC)
)

@Composable
fun FocusFlowTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    customAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = when (themeMode) {
        ThemeMode.MATERIAL_YOU -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                BaseDarkColorScheme
            }
        }
        ThemeMode.LIGHT -> BaseLightColorScheme
        ThemeMode.AMOLED -> AmoledColorScheme
        ThemeMode.DARK -> BaseDarkColorScheme
    }

    val finalColorScheme = if (customAccentColor != null && themeMode != ThemeMode.MATERIAL_YOU) {
        baseScheme.copy(primary = customAccentColor)
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}


