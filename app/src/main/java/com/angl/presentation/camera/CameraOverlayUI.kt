package com.angl.presentation.camera

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angl.domain.engine.*
import com.angl.presentation.theme.*

/**
 * High-Fashion Tech UI Overlay System for Angl Camera App
 * 
 * This file contains all premium UI components for the camera overlay:
 * - GlassModifier: Frosted glass effect with blur (Android 12+)
 * - GuidanceHUD: Animated feedback box with elegant typography
 * - ARCanvas: Full-screen visual guidance with reticle and arrows
 * - CameraScreenContent: Complete camera screen layout
 * 
 * Design Philosophy: "Dark Luxury" - Sophisticated, fluid, premium feel
 */

// ============================================================================
// 1. HELPER: GlassModifier Extension
// ============================================================================

/**
 * Creates a high-quality frosted glass effect modifier.
 * 
 * Features:
 * - Vertical gradient (Black alpha 0.4 -> 0.8)
 * - Subtle white border (alpha 0.10)
 * - 24.dp corner radius
 * - Blur effect on Android 12+ (RenderEffect)
 * - Fallback to higher opacity on older Android versions
 */
fun Modifier.glassBackground(): Modifier {
    val cornerRadius = 24.dp
    val borderColor = Color.White.copy(alpha = 0.10f)
    
    return this
        .clip(RoundedCornerShape(cornerRadius))
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ with blur effect
                this
                    .graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                12f, // radiusX
                                12f, // radiusY
                                Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
            } else {
                // Fallback for older Android versions (higher opacity)
                this.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
            }
        )
        .border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(cornerRadius)
        )
}

// ============================================================================
// 2. COMPONENT: GuidanceHUD (The Feedback Box)
// ============================================================================

/**
 * Premium guidance HUD that floats above the shutter button.
 * 
 * Features:
 * - Frosted glass background
 * - Main instruction in Cinzel Bold (uppercase, large)
 * - Sub-instruction in Montserrat Medium (small, wide letter spacing)
 * - Color animates: ErrorRed (Critical) -> ElectricGold (Warning) -> NeonLime (Perfect)
 * - Slide up & fade in animation on text change
 */
@Composable
fun GuidanceHUD(
    feedbackState: FeedbackStateExtended,
    modifier: Modifier = Modifier
) {
    // Animate color based on feedback state
    val targetColor = when (feedbackState) {
        is FeedbackStateExtended.Perfect -> NeonLime
        is FeedbackStateExtended.Warning -> ElectricGold
        is FeedbackStateExtended.Critical -> ErrorRed
    }
    
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "hud_color"
    )
    
    // Extract messages
    val mainMessage = when (feedbackState) {
        is FeedbackStateExtended.Perfect -> "PERFECT"
        is FeedbackStateExtended.Warning -> feedbackState.message
        is FeedbackStateExtended.Critical -> feedbackState.message
    }
    
    val subMessage = when (feedbackState) {
        is FeedbackStateExtended.Perfect -> "TAKE THE SHOT"
        is FeedbackStateExtended.Warning -> "ADJUST COMPOSITION"
        is FeedbackStateExtended.Critical -> "IMMEDIATE ACTION REQUIRED"
    }
    
    Box(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .glassBackground()
            .padding(vertical = 20.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main instruction with animated content
            AnimatedContent(
                targetState = mainMessage,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(),
                        initialContentExit = slideOutVertically(
                            targetOffsetY = { -it / 2 },
                            animationSpec = tween(300)
                        ) + fadeOut(),
                        sizeTransform = SizeTransform(clip = false)
                    )
                },
                label = "main_message_animation"
            ) { message ->
                Text(
                    text = message.uppercase(),
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = animatedColor,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sub-instruction with animated content
            AnimatedContent(
                targetState = subMessage,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(
                            animationSpec = tween(400, delayMillis = 100)
                        ),
                        initialContentExit = fadeOut(
                            animationSpec = tween(200)
                        )
                    )
                },
                label = "sub_message_animation"
            ) { message ->
                Text(
                    text = message.uppercase(),
                    fontFamily = MontserratFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = OffWhite.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

// ============================================================================
// 3. COMPONENT: ARCanvas (The Visuals)
// ============================================================================

/**
 * Full-screen AR canvas overlay with visual guidance.
 * 
 * Features:
 * - Center reticle (elegant viewfinder corners)
 * - Perfect state: Corners snap inward with spring animation, glow NeonLime
 * - Directional arrows: Pulsating chevrons using drawPath
 * - Smooth, fluid animations for premium feel
 */
@Composable
fun ARCanvas(
    feedbackState: FeedbackStateExtended,
    modifier: Modifier = Modifier
) {
    // Animate color based on state
    val targetColor = when (feedbackState) {
        is FeedbackStateExtended.Perfect -> NeonLime
        is FeedbackStateExtended.Warning -> ElectricGold
        is FeedbackStateExtended.Critical -> ErrorRed
    }
    
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "canvas_color"
    )
    
    // Spring animation for Perfect state reticle
    val isPerfect = feedbackState is FeedbackStateExtended.Perfect
    val reticleScale by animateFloatAsState(
        targetValue = if (isPerfect) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "reticle_scale"
    )
    
    // Pulsating animation for arrows
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_pulse")
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )
    
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrow_offset"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val reticleSize = 120f * reticleScale
        val cornerLength = reticleSize * 0.35f
        val strokeWidth = if (isPerfect) 4f else 3f
        
        // Draw the reticle (four corners)
        drawReticleCorners(
            center = Offset(centerX, centerY),
            size = reticleSize,
            cornerLength = cornerLength,
            color = animatedColor,
            strokeWidth = strokeWidth
        )
        
        // Draw glow effect for Perfect state
        if (isPerfect) {
            drawCircle(
                color = animatedColor.copy(alpha = 0.2f),
                radius = reticleSize * 1.3f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )
        }
        
        // Draw directional arrows based on guidance type
        when (feedbackState) {
            is FeedbackStateExtended.Warning,
            is FeedbackStateExtended.Critical -> {
                val guidanceType = when (feedbackState) {
                    is FeedbackStateExtended.Warning -> feedbackState.guidanceType
                    is FeedbackStateExtended.Critical -> feedbackState.guidanceType
                    else -> GuidanceType.None
                }
                
                when (guidanceType) {
                    is GuidanceType.Translation -> {
                        drawDirectionalArrows(
                            direction = guidanceType.direction,
                            color = animatedColor.copy(alpha = arrowAlpha),
                            offset = arrowOffset,
                            size = size
                        )
                    }
                    else -> { /* No directional arrows */ }
                }
            }
            else -> { /* Perfect state - no arrows */ }
        }
    }
}

/**
 * Draws elegant reticle corners (viewfinder frame).
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawReticleCorners(
    center: Offset,
    size: Float,
    cornerLength: Float,
    color: Color,
    strokeWidth: Float
) {
    val halfSize = size / 2
    
    // Top-left corner
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y - halfSize),
        end = Offset(center.x - halfSize + cornerLength, center.y - halfSize),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y - halfSize),
        end = Offset(center.x - halfSize, center.y - halfSize + cornerLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Top-right corner
    drawLine(
        color = color,
        start = Offset(center.x + halfSize, center.y - halfSize),
        end = Offset(center.x + halfSize - cornerLength, center.y - halfSize),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x + halfSize, center.y - halfSize),
        end = Offset(center.x + halfSize, center.y - halfSize + cornerLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Bottom-left corner
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y + halfSize),
        end = Offset(center.x - halfSize + cornerLength, center.y + halfSize),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y + halfSize),
        end = Offset(center.x - halfSize, center.y + halfSize - cornerLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Bottom-right corner
    drawLine(
        color = color,
        start = Offset(center.x + halfSize, center.y + halfSize),
        end = Offset(center.x + halfSize - cornerLength, center.y + halfSize),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x + halfSize, center.y + halfSize),
        end = Offset(center.x + halfSize, center.y + halfSize - cornerLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

/**
 * Draws pulsating directional arrows (chevrons) using drawPath.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDirectionalArrows(
    direction: TranslationDirection,
    color: Color,
    offset: Float,
    size: Size
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val arrowSize = 60f
    
    // Determine position and rotation based on direction
    when (direction) {
        TranslationDirection.LEFT -> {
            // Draw chevrons pointing left (on the left side of screen)
            for (i in 0..2) {
                val alpha = (1f - i * 0.3f).coerceAtLeast(0f)
                val x = centerX - 200f - offset - (i * 50f)
                drawChevronPath(
                    center = Offset(x, centerY),
                    size = arrowSize,
                    rotation = 180f,
                    color = color.copy(alpha = alpha)
                )
            }
        }
        TranslationDirection.RIGHT -> {
            // Draw chevrons pointing right (on the right side of screen)
            for (i in 0..2) {
                val alpha = (1f - i * 0.3f).coerceAtLeast(0f)
                val x = centerX + 200f + offset + (i * 50f)
                drawChevronPath(
                    center = Offset(x, centerY),
                    size = arrowSize,
                    rotation = 0f,
                    color = color.copy(alpha = alpha)
                )
            }
        }
        TranslationDirection.UP -> {
            // Draw chevrons pointing up (at the top of screen)
            for (i in 0..2) {
                val alpha = (1f - i * 0.3f).coerceAtLeast(0f)
                val y = centerY - 200f - offset - (i * 50f)
                drawChevronPath(
                    center = Offset(centerX, y),
                    size = arrowSize,
                    rotation = -90f,
                    color = color.copy(alpha = alpha)
                )
            }
        }
        TranslationDirection.DOWN -> {
            // Draw chevrons pointing down (at the bottom of screen)
            for (i in 0..2) {
                val alpha = (1f - i * 0.3f).coerceAtLeast(0f)
                val y = centerY + 200f + offset + (i * 50f)
                drawChevronPath(
                    center = Offset(centerX, y),
                    size = arrowSize,
                    rotation = 90f,
                    color = color.copy(alpha = alpha)
                )
            }
        }
        else -> { /* FORWARD/BACK not implemented with arrows */ }
    }
}

/**
 * Draws a single chevron (>) using Path.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChevronPath(
    center: Offset,
    size: Float,
    rotation: Float,
    color: Color
) {
    val path = Path().apply {
        val halfSize = size / 2
        // Start at top-left
        moveTo(center.x - halfSize * 0.6f, center.y - halfSize)
        // Line to tip (right point)
        lineTo(center.x + halfSize * 0.6f, center.y)
        // Line to bottom-left
        lineTo(center.x - halfSize * 0.6f, center.y + halfSize)
    }
    
    // Apply rotation
    val radians = rotation * Math.PI.toFloat() / 180f
    val cos = kotlin.math.cos(radians)
    val sin = kotlin.math.sin(radians)
    
    path.translate(Offset(-center.x, -center.y))
    val rotationMatrix = Matrix()
    rotationMatrix.rotateZ(rotation)
    path.transform(rotationMatrix)
    path.translate(Offset(center.x, center.y))
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

// ============================================================================
// 4. LAYOUT: CameraScreenContent
// ============================================================================

/**
 * Complete camera screen layout with all layers assembled.
 * 
 * Layer hierarchy (bottom to top):
 * 1. Camera Preview (fills screen)
 * 2. ARCanvas overlay (fills screen)
 * 3. Top gradient scrim (protects status bar)
 * 4. GuidanceHUD (floats ~150dp from bottom)
 * 5. ShutterButton (bottom center)
 */
@Composable
fun CameraScreenContent(
    feedbackState: FeedbackStateExtended,
    onTakePhoto: () -> Unit,
    cameraPreviewContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Camera Preview (provided by caller)
        cameraPreviewContent()
        
        // Layer 2: AR Canvas Overlay
        ARCanvas(
            feedbackState = feedbackState,
            modifier = Modifier.fillMaxSize()
        )
        
        // Layer 3: Top gradient scrim for status bar visibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Layer 4: Guidance HUD (floats above shutter button)
        GuidanceHUD(
            feedbackState = feedbackState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp)
        )
        
        // Layer 5: Shutter Button (bottom center)
        ShutterButton(
            onClick = onTakePhoto,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}
