package com.angl.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism UI Components - "Frosted Glass" Effect
 * 
 * High-end design pattern used by Apple, Windows 11, and premium apps
 * Creates a semi-transparent, blurred background effect for overlays
 */

/**
 * Applies a "frosted glass" background to any composable
 * 
 * This creates a gradient from semi-transparent black (60% opacity at top)
 * to more opaque black (80% opacity at bottom), giving a subtle depth effect
 * 
 * Usage:
 * ```
 * Box(
 *     modifier = Modifier
 *         .glassBackground()
 *         .padding(16.dp)
 * ) {
 *     Text("Your content here")
 * }
 * ```
 */
fun Modifier.glassBackground() = this
    .clip(RoundedCornerShape(16.dp))  // Rounded corners for modern aesthetic
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.6f),  // 60% opacity at top
                Color.Black.copy(alpha = 0.8f)   // 80% opacity at bottom
            )
        )
    )
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.1f),  // Subtle white border for definition
        shape = RoundedCornerShape(16.dp)
    )

/**
 * Alternative glass effect with custom opacity
 * 
 * @param topAlpha Opacity at the top of the gradient (0.0 to 1.0)
 * @param bottomAlpha Opacity at the bottom of the gradient (0.0 to 1.0)
 */
fun Modifier.glassBackground(topAlpha: Float = 0.6f, bottomAlpha: Float = 0.8f) = this
    .clip(RoundedCornerShape(16.dp))
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = topAlpha),
                Color.Black.copy(alpha = bottomAlpha)
            )
        )
    )
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    )

/**
 * Glass effect with blur (requires API 31+)
 * Creates the most authentic "frosted glass" appearance
 * Falls back to standard glass effect on older devices
 */
fun Modifier.glassBackgroundWithBlur(
    topAlpha: Float = 0.6f,
    bottomAlpha: Float = 0.8f,
    blurRadius: Float = 25f
) = this
    .blur(blurRadius.dp)  // Blur the content behind (API 31+)
    .clip(RoundedCornerShape(16.dp))
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = topAlpha),
                Color.Black.copy(alpha = bottomAlpha)
            )
        )
    )
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.15f),  // Slightly more visible with blur
        shape = RoundedCornerShape(16.dp)
    )

/**
 * Scrim background for full-screen overlays
 * Uses the GlassScrim color from the color palette
 */
fun Modifier.glassScrim() = this
    .background(GlassScrim)
