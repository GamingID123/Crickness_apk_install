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

private val CricknessDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color.Black,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    tertiary = NeonPink,
    background = DarkCanvas,
    surface = DarkSurface,
    onBackground = TextWhite,
    onSurface = TextWhite
)

private val CricknessLightColorScheme = lightColorScheme(
    primary = Color(0xFF00A843),
    onPrimary = Color.White,
    secondary = Color(0xFF00838F),
    background = Color(0xFFF6F8FA),
    surface = Color.White,
    onBackground = Color(0xFF24292F),
    onSurface = Color(0xFF24292F)
)

@Composable
fun CricknessTheme(
    darkTheme: Boolean = true, // Default dark theme for Crickness
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CricknessDarkColorScheme
        else -> CricknessLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
