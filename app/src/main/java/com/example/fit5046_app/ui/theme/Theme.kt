package com.example.fit5046_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimary,
    secondary = AppPrimaryDark,
    tertiary = AppTertiary,
)

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary, // Main teal
    secondary = AppPrimaryDark, // Dark teal for Title text on white background
    tertiary = AppTertiary, // Orange for small highlights
    primaryContainer = Color(0xFFcdede7), // Light teal
    secondaryContainer = Color(0xFFcdede7),
    background = Color(0xFFf0f0f1), // Light gray for background
    onBackground = Color.DarkGray,
    surface = Color(0xFFffffff) // White for cards and other surface

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FIT5046_appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}