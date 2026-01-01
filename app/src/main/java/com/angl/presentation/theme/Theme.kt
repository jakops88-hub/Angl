package com.angl.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark Luxury Theme - "The Angl Aesthetic"
 * Forces dark mode for premium camera app experience
 * Transparent status bar with light icons
 */

// Dark theme - Primary use case for camera apps (Dark Luxury Aesthetic)
private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    primaryContainer = Color(0xFF8BC34A),  // Slightly muted lime
    secondary = ElectricGold,
    secondaryContainer = Color(0xFFFFE082),  // Lighter gold
    tertiary = ElectricPurple,
    tertiaryContainer = Color(0xFFBA68C8),  // Lighter purple
    background = RichBlack,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF2A2A2A),
    error = ErrorRed,
    onPrimary = RichBlack,
    onSecondary = RichBlack,
    onTertiary = PureWhite,
    onBackground = OffWhite,
    onSurface = OffWhite,
    onSurfaceVariant = Color(0xFFB0B0B0),
    onError = PureWhite,
    outline = Color(0xFF3D3D3D),
    outlineVariant = Color(0xFF2A2A2A)
)

// Light theme - Available but camera apps primarily use dark
private val LightColorScheme = lightColorScheme(
    primary = NeonLime,
    primaryContainer = Color(0xFFE6FF80),
    secondary = ElectricGold,
    secondaryContainer = Color(0xFFFFE082),
    tertiary = ElectricPurple,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    error = ErrorRed,
    onPrimary = RichBlack,
    onSecondary = RichBlack,
    onTertiary = PureWhite,
    onBackground = RichBlack,
    onSurface = RichBlack,
    onError = PureWhite
)

@Composable
fun AnglTheme(
    darkTheme: Boolean = true,  // FORCE DARK MODE - Ignore system settings
    content: @Composable () -> Unit
) {
    // Always use dark theme for camera app aesthetic
    val colorScheme = DarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Transparent status bar for immersive experience
            window.statusBarColor = Color.Transparent.toArgb()
            // Light icons on transparent/dark status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
