package com.aiham.dailycompanion

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppPurple = Color(0xFF6B5AE7)
val AppPurpleDark = Color(0xFF4D3FC4)
val AppLavender = Color(0xFFEDE9FF)
val AppBackground = Color(0xFFF8F7FC)
val AppMuted = Color(0xFF6E697A)
val AppGreen = Color(0xFF36AD79)
val AppAmber = Color(0xFFF1A936)
val AppRed = Color(0xFFE15B65)
val AppSky = Color(0xFFE8F4FF)

private val LightColors = lightColorScheme(
    primary = AppPurple,
    onPrimary = Color.White,
    primaryContainer = AppLavender,
    onPrimaryContainer = Color(0xFF21175E),
    secondary = AppGreen,
    onSecondary = Color.White,
    background = Color(0xFFF9F8FD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1EFF7),
    onBackground = Color(0xFF1D1B23),
    onSurface = Color(0xFF1D1B23),
    onSurfaceVariant = Color(0xFF5F596A),
    outline = Color(0xFFD7D2DF),
    error = AppRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC3B9FF),
    onPrimary = Color(0xFF2A1E6C),
    primaryContainer = Color(0xFF3B3374),
    onPrimaryContainer = Color(0xFFF0ECFF),
    secondary = Color(0xFF7DDBAF),
    onSecondary = Color(0xFF073824),
    background = Color(0xFF111015),
    surface = Color(0xFF1A191F),
    surfaceVariant = Color(0xFF242229),
    onBackground = Color(0xFFF0EDF3),
    onSurface = Color(0xFFF0EDF3),
    onSurfaceVariant = Color(0xFFC9C2D0),
    outline = Color(0xFF4A4652),
    error = Color(0xFFFFB4AB)
)

private val AppTypography = Typography().copy(
    headlineSmall = Typography().headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.sp),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    bodyLarge = Typography().bodyLarge.copy(lineHeight = 24.sp),
    bodyMedium = Typography().bodyMedium.copy(lineHeight = 21.sp)
)

@Composable
fun DailyCompanionTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
