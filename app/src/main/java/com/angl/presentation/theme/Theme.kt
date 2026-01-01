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

// Dark theme - Primary use case for camera apps (Luxury Aesthetic)
private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    primaryContainer = PrimaryVariant,
    secondary = CoolCyan,
    secondaryContainer = SecondaryVariant,
    tertiary = ElectricPurple,
    background = PureBlack,
    surface = DeepCharcoal,
    error = ErrorRed,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onTertiary = Color(0xFFFFFFFF),
    onBackground = NeonLime,
    onSurface = Color(0xFFFFFFFF),
    onError = Color(0xFFFFFFFF)
)

// Light theme - Available but camera apps primarily use dark
private val LightColorScheme = lightColorScheme(
    primary = NeonLime,
    primaryContainer = Color(0xFFE6FF80),
    secondary = CoolCyan,
    secondaryContainer = Color(0xFF80DEEA),
    tertiary = ElectricPurple,
    background = Color(0xFFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    error = ErrorRed,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onTertiary = Color(0xFFFFFFFF),
    onBackground = PureBlack,
    onSurface = PureBlack,
    onError = Color(0xFFFFFFFF)
)

@Composable
fun AnglTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
