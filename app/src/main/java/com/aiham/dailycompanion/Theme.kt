package com.aiham.dailycompanion

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppPurple = Color(0xFF6C5CE7)
val AppPurpleDark = Color(0xFF4E3FC4)
val AppLavender = Color(0xFFEDE9FF)
val AppBackground = Color(0xFFF8F7FC)
val AppMuted = Color(0xFF777386)
val AppGreen = Color(0xFF42B883)
val AppAmber = Color(0xFFF3AA36)
val AppRed = Color(0xFFE55F67)
val AppSky = Color(0xFFE8F4FF)

private val LightColors = lightColorScheme(
    primary = AppPurple,
    onPrimary = Color.White,
    primaryContainer = AppLavender,
    onPrimaryContainer = Color(0xFF241A66),
    secondary = AppGreen,
    background = AppBackground,
    surface = Color.White,
    onBackground = Color(0xFF1C1B20),
    onSurface = Color(0xFF1C1B20),
    error = AppRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9AEFF),
    onPrimary = Color(0xFF2F236E),
    primaryContainer = Color(0xFF473B91),
    onPrimaryContainer = Color(0xFFE8E2FF),
    secondary = Color(0xFF7DDBAF),
    background = Color(0xFF121116),
    surface = Color(0xFF1B1A20),
    onBackground = Color(0xFFE9E6ED),
    onSurface = Color(0xFFE9E6ED),
    error = Color(0xFFFFB4AB)
)

@Composable
fun DailyCompanionTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
