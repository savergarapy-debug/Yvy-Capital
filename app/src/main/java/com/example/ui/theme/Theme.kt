package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ============================================================================
// YVY CAPITAL — TOKENS DE FORMA, ESPACIADO Y ANIMACIÓN
// ============================================================================

object YvySpacing {
    val GridUnit = 8.dp
    val ScreenHorizontal = 20.dp
    val SectionGap = 28.dp
    val SectionGapLarge = 32.dp
    val CardPadding = 16.dp
    val ItemSpacing = 12.dp
}

object YvyShapes {
    val Card = RoundedCornerShape(12.dp)
    val FieldAndButton = RoundedCornerShape(10.dp)
    val Badge = RoundedCornerShape(6.dp)
    val Chip = CircleShape
}

object YvyMotion {
    const val DurationShortMs = 150
    const val DurationMediumMs = 200
    const val DurationLongMs = 250
    const val DurationFast = 150
    const val DurationMedium = 200
    const val DurationSlow = 250
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

private val LightColors = lightColorScheme(
    primary = YvyMonteGreen,
    onPrimary = Color.White,
    primaryContainer = YvyMonteGreenSoft,
    onPrimaryContainer = YvyTextPrimaryLight,
    secondary = YvyTerracotta,
    onSecondary = Color.White,
    secondaryContainer = YvyTerracottaSoft,
    onSecondaryContainer = YvyTextPrimaryLight,
    tertiary = YvyProGold,
    onTertiary = YvyTextPrimaryLight,
    tertiaryContainer = YvyProGoldSoft,
    onTertiaryContainer = YvyTextPrimaryLight,
    background = YvyBackgroundLight,
    onBackground = YvyTextPrimaryLight,
    surface = YvySurfaceLight,
    onSurface = YvyTextPrimaryLight,
    surfaceVariant = YvyPlaceholderLight,
    onSurfaceVariant = YvyTextSecondaryLight,
    outline = YvyBorderLight,
    outlineVariant = YvyBorderLight,
    error = YvyError,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = YvyDarkMonteGreen,
    onPrimary = YvyDarkBackground,
    primaryContainer = YvyDarkCard,
    onPrimaryContainer = YvyDarkTextPrimary,
    secondary = YvyDarkTerracotta,
    onSecondary = YvyDarkBackground,
    secondaryContainer = YvyDarkCard,
    onSecondaryContainer = YvyDarkTextPrimary,
    tertiary = YvyProGold,
    onTertiary = YvyDarkBackground,
    tertiaryContainer = YvyDarkCard,
    onTertiaryContainer = YvyDarkTextPrimary,
    background = YvyDarkBackground,
    onBackground = YvyDarkTextPrimary,
    surface = YvyDarkSurface,
    onSurface = YvyDarkTextPrimary,
    surfaceVariant = YvyDarkCard,
    onSurfaceVariant = YvyDarkTextSecondary,
    outline = YvyDarkBorder,
    outlineVariant = YvyDarkBorder,
    error = Color(0xFFFF897D),
    onError = YvyDarkBackground
)

@Composable
fun YvyCapitalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    YvyCapitalTheme(darkTheme = darkTheme, content = content)
}
