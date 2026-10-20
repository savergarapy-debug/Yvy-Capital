package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = YvyForestPrimary,
    onPrimary = Color.White,
    primaryContainer = YvySageLight,
    onPrimaryContainer = YvyForestDeep,
    secondary = YvyGoldAccent,
    onSecondary = YvyForestDeep,
    secondaryContainer = YvyGoldLight,
    onSecondaryContainer = YvyForestDeep,
    tertiary = YvyForestMedium,
    onTertiary = Color.White,
    background = YvyBeigeWarm,
    onBackground = YvyCharcoal,
    surface = YvyBeigeSurface,
    onSurface = YvyCharcoal,
    surfaceVariant = YvySandCard,
    onSurfaceVariant = YvySlate,
    outline = YvyBorder,
    error = YvyError,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = YvyGreenBright,
    onPrimary = YvyForestDeep,
    primaryContainer = YvyForestMedium,
    onPrimaryContainer = YvyBeigeText,
    secondary = YvyGoldAccent,
    onSecondary = YvyForestDeep,
    secondaryContainer = YvyDarkCard,
    onSecondaryContainer = YvyGoldLight,
    tertiary = YvyGoldAccent,
    onTertiary = YvyForestDeep,
    background = YvyDarkBackground,
    onBackground = YvyBeigeText,
    surface = YvyDarkSurface,
    onSurface = YvyBeigeText,
    surfaceVariant = YvyDarkCard,
    onSurfaceVariant = YvySageLight,
    outline = YvyForestMedium,
    error = Color(0xFFFF897D),
    onError = YvyForestDeep
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
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    YvyCapitalTheme(darkTheme = darkTheme, content = content)
}
