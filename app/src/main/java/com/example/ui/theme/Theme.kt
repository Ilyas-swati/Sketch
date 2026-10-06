package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SketchOrange,
    onPrimary = Color.Black,
    primaryContainer = SketchDarkSurfaceElevated,
    onPrimaryContainer = SketchOrangeLight,
    secondary = SketchCyan,
    onSecondary = Color.Black,
    secondaryContainer = SketchDarkSurfaceBorder,
    onSecondaryContainer = Color.White,
    tertiary = SketchGreen,
    background = SketchDarkBackground,
    onBackground = TextPrimary,
    surface = SketchDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = SketchDarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SketchDarkSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dedicated modern dark aesthetic for artistic sketching experience
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
