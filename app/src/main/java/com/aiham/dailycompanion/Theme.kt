package com.aiham.dailycompanion

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppPurple = Color(0xFF6F63E8)
val AppPurpleDark = Color(0xFF5145C9)
val AppLavender = Color(0xFFF0EDFF)
val AppBackground = Color(0xFFF8F8FC)
val AppText = Color(0xFF20202A)
val AppMuted = Color(0xFF777786)
val Mint = Color(0xFFE8F8EF)
val Peach = Color(0xFFFFF0E7)
val Sky = Color(0xFFEAF4FF)
val Butter = Color(0xFFFFF7D9)
val Rose = Color(0xFFFFEDF1)

private val LightColors = lightColorScheme(
    primary = AppPurple,
    onPrimary = Color.White,
    primaryContainer = AppLavender,
    onPrimaryContainer = AppPurpleDark,
    secondary = Color(0xFF35B979),
    tertiary = Color(0xFFF0A824),
    background = AppBackground,
    onBackground = AppText,
    surface = Color.White,
    onSurface = AppText,
    surfaceVariant = Color(0xFFF0F0F5),
    onSurfaceVariant = AppMuted,
    outline = Color(0xFFE2E2EA)
)

@Composable
fun DailyCompanionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
