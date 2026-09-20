package ru.vahta.calculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppBackground = Color(0xFF121212)
val AppSurface = Color(0xFF181D21)
val AppSurfaceAlt = Color(0xFF20262B)
val InputSurface = Color(0xFF151A1E)
val AppBorder = Color(0xFF353D43)
val AppOrange = Color(0xFFFF6B00)
val MoneyGreen = Color(0xFF4CE67A)
val MutedText = Color(0xFF9DA8B2)

private val AppColors = darkColorScheme(
    primary = AppOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3A210E),
    onPrimaryContainer = Color.White,
    secondary = MoneyGreen,
    onSecondary = Color(0xFF07140B),
    secondaryContainer = Color(0xFF0C2E17),
    onSecondaryContainer = Color(0xFFD8FFE3),
    tertiary = Color(0xFF6F9EE8),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF193A66),
    onTertiaryContainer = Color(0xFFD9E8FF),
    background = AppBackground,
    onBackground = Color(0xFFF4F5F6),
    surface = AppSurface,
    onSurface = Color(0xFFF4F5F6),
    surfaceVariant = AppSurfaceAlt,
    onSurfaceVariant = MutedText,
    outline = AppBorder,
    error = Color(0xFFFF6B6B),
)

@Composable
fun VahtaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        content = content,
    )
}
