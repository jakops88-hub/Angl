package com.angl.presentation.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angl.domain.engine.*

/**
 * AnglOverlay is a professional-grade AR overlay for real-time photo composition guidance.
 * 
 * This overlay provides:
 * - Animated visual cues (chevrons, arcs, reticles)
 * - Readable text guidance with scrim backgrounds
 * - Smooth state transitions with spring animations
 * - Color-coded feedback (amber, red, green)
 * 
 * Design Philosophy:
 * - Motion conveys meaning immediately
 * - Legible against any background
 * - Reactive and alive, not static
 * - Professional-grade feel
 */
@Composable
fun AnglOverlay(
    feedbackState: FeedbackStateExtended,
    modifier: Modifier = Modifier
) {
    // Animate color based on state
    val targetColor = when (feedbackState) {
        is FeedbackStateExtended.Perfect -> Color(0xFF00C853) // Green
        is FeedbackStateExtended.Warning -> Color(0xFFFF6F00) // Amber/Orange
        is FeedbackStateExtended.Critical -> Color(0xFFD50000) // Red
    }
    
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "feedback_color"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // AR Visual Guidance (center of screen)
        ARGuidanceLayer(
            feedbackState = feedbackState,
            color = animatedColor
        )

        // Text Guidance (bottom of screen)
        TextGuidanceBanner(
            feedbackState = feedbackState,
            color = animatedColor,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Renders AR visual guidance based on feedback state.
 */
@Composable
private fun ARGuidanceLayer(
    feedbackState: FeedbackStateExtended,
    color: Color
) {
    when (feedbackState) {
        is FeedbackStateExtended.Perfect -> {
            PerfectStateReticle(color = color)
        }
        is FeedbackStateExtended.Warning -> {
            when (val guidance = feedbackState.guidanceType) {
                is GuidanceType.Translation -> {
                    TranslationChevrons(
                        direction = guidance.direction,
                        color = color,
                        intensity = 0.7f
                    )
                }
                is GuidanceType.Rotation -> {
                    RotationArc(
                        direction = guidance.direction,
                        color = color,
                        intensity = 0.7f
                    )
                }
                GuidanceType.None -> {
                    // No visual guidance, just text
                }
            }
        }
        is FeedbackStateExtended.Critical -> {
            when (val guidance = feedbackState.guidanceType) {
                is GuidanceType.Translation -> {
                    TranslationChevrons(
                        direction = guidance.direction,
                        color = color,
                        intensity = 1.0f
                    )
                }
                is GuidanceType.Rotation -> {
                    RotationArc(
                        direction = guidance.direction,
                        color = color,
                        intensity = 1.0f
                    )
                }
                GuidanceType.None -> {
                    // Critical state without specific guidance
                }
            }
        }
    }
}

/**
 * Perfect state reticle - "target acquired" animation.
 */
@Composable
private fun PerfectStateReticle(color: Color) {
    // Animate scale for "snap on" effect
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reticle_scale"
    )
    
    // Pulsing glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "reticle_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val reticleSize = 200f * scale

        // Draw outer glow
        drawCircle(
            color = color.copy(alpha = glowAlpha * 0.3f),
            radius = reticleSize * 1.2f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3f)
        )

        // Draw main reticle frame (4 corners)
        val cornerLength = reticleSize * 0.3f
        val strokeWidth = 8f

        // Top-left corner
        drawLine(
            color = color,
            start = Offset(centerX - reticleSize, centerY - reticleSize),
            end = Offset(centerX - reticleSize + cornerLength, centerY - reticleSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX - reticleSize, centerY - reticleSize),
            end = Offset(centerX - reticleSize, centerY - reticleSize + cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Top-right corner
        drawLine(
            color = color,
            start = Offset(centerX + reticleSize, centerY - reticleSize),
            end = Offset(centerX + reticleSize - cornerLength, centerY - reticleSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX + reticleSize, centerY - reticleSize),
            end = Offset(centerX + reticleSize, centerY - reticleSize + cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Bottom-left corner
        drawLine(
            color = color,
            start = Offset(centerX - reticleSize, centerY + reticleSize),
            end = Offset(centerX - reticleSize + cornerLength, centerY + reticleSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX - reticleSize, centerY + reticleSize),
            end = Offset(centerX - reticleSize, centerY + reticleSize - cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Bottom-right corner
        drawLine(
            color = color,
            start = Offset(centerX + reticleSize, centerY + reticleSize),
            end = Offset(centerX + reticleSize - cornerLength, centerY + reticleSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX + reticleSize, centerY + reticleSize),
            end = Offset(centerX + reticleSize, centerY + reticleSize - cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Center crosshair
        val crosshairSize = 20f
        drawLine(
            color = color,
            start = Offset(centerX - crosshairSize, centerY),
            end = Offset(centerX + crosshairSize, centerY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, centerY - crosshairSize),
            end = Offset(centerX, centerY + crosshairSize),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Translation chevrons - animated arrows showing direction to move.
 */
@Composable
private fun TranslationChevrons(
    direction: TranslationDirection,
    color: Color,
    intensity: Float
) {
    // Spring animation for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "chevron_pulse")
    
    val pulseSpeed = if (intensity > 0.8f) 400 else 600
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chevron_offset"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chevron_alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Determine rotation based on direction
        val rotation = when (direction) {
            TranslationDirection.RIGHT -> 0f
            TranslationDirection.LEFT -> 180f
            TranslationDirection.UP -> -90f
            TranslationDirection.DOWN -> 90f
            TranslationDirection.FORWARD -> 0f // Special case, draw differently
            TranslationDirection.BACK -> 180f // Special case
        }

        rotate(rotation, pivot = Offset(centerX, centerY)) {
            // Draw 3 chevrons with increasing offset
            for (i in 0..2) {
                val chevronAlpha = alpha - (i * 0.3f)
                if (chevronAlpha > 0) {
                    drawChevron(
                        center = Offset(centerX + offset + (i * 60f), centerY),
                        color = color.copy(alpha = chevronAlpha.coerceIn(0f, 1f)),
                        size = 80f,
                        strokeWidth = 12f * intensity
                    )
                }
            }
        }
    }
}

/**
 * Draws a single chevron (>) shape.
 */
private fun DrawScope.drawChevron(
    center: Offset,
    color: Color,
    size: Float,
    strokeWidth: Float
) {
    val halfSize = size / 2
    
    // Top line of chevron
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y - halfSize),
        end = Offset(center.x, center.y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Bottom line of chevron
    drawLine(
        color = color,
        start = Offset(center.x, center.y),
        end = Offset(center.x - halfSize, center.y + halfSize),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

/**
 * Rotation arc - curved arrow showing rotation direction.
 */
@Composable
private fun RotationArc(
    direction: RotationDirection,
    color: Color,
    intensity: Float
) {
    // Animate arc sweep
    val infiniteTransition = rememberInfiniteTransition(label = "arc_sweep")
    
    val sweepSpeed = if (intensity > 0.8f) 800 else 1200
    
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(sweepSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arc_sweep_angle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = 150f
        
        // Determine start angle based on direction
        val startAngle = when (direction) {
            RotationDirection.TILT_LEFT -> 45f
            RotationDirection.TILT_RIGHT -> -135f
        }
        
        val clockwise = direction == RotationDirection.TILT_RIGHT

        // Draw arc
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = if (clockwise) sweepAngle else -sweepAngle,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 10f * intensity, cap = StrokeCap.Round)
        )

        // Draw arrowhead at end of arc
        if (sweepAngle > 30f) {
            val endAngle = startAngle + if (clockwise) sweepAngle else -sweepAngle
            val endAngleRad = Math.toRadians(endAngle.toDouble())
            val arrowX = centerX + radius * kotlin.math.cos(endAngleRad).toFloat()
            val arrowY = centerY + radius * kotlin.math.sin(endAngleRad).toFloat()
            
            val arrowSize = 30f
            val arrowAngle = endAngleRad + if (clockwise) Math.PI / 2 else -Math.PI / 2
            
            // Arrow tip
            val tipX = arrowX + arrowSize * kotlin.math.cos(arrowAngle).toFloat()
            val tipY = arrowY + arrowSize * kotlin.math.sin(arrowAngle).toFloat()
            
            // Arrow wings
            val wing1X = arrowX + (arrowSize * 0.6f) * kotlin.math.cos(arrowAngle + 0.5).toFloat()
            val wing1Y = arrowY + (arrowSize * 0.6f) * kotlin.math.sin(arrowAngle + 0.5).toFloat()
            val wing2X = arrowX + (arrowSize * 0.6f) * kotlin.math.cos(arrowAngle - 0.5).toFloat()
            val wing2Y = arrowY + (arrowSize * 0.6f) * kotlin.math.sin(arrowAngle - 0.5).toFloat()
            
            drawLine(
                color = color,
                start = Offset(wing1X, wing1Y),
                end = Offset(tipX, tipY),
                strokeWidth = 10f * intensity,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(tipX, tipY),
                end = Offset(wing2X, wing2Y),
                strokeWidth = 10f * intensity,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Text guidance banner with dark gradient scrim for readability.
 */
@Composable
private fun TextGuidanceBanner(
    feedbackState: FeedbackStateExtended,
    color: Color,
    modifier: Modifier = Modifier
) {
    val message = when (feedbackState) {
        is FeedbackStateExtended.Perfect -> "PERFECT - TAKE THE SHOT"
        is FeedbackStateExtended.Warning -> feedbackState.message
        is FeedbackStateExtended.Critical -> feedbackState.message
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = message,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
