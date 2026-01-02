package com.angl.presentation.camera

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Premium camera shutter button with classic design.
 * 
 * Features:
 * - Large white ring with smaller white circle inside (classic camera look)
 * - Scale animation on press for tactile feedback
 * - Clean, minimalist design
 * 
 * @param onClick Callback when the button is clicked
 * @param modifier Modifier for positioning and styling
 */
@Composable
fun ShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate scale when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "shutter_scale"
    )
    
    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer white ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(
                    width = 4.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner white circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
            )
        }
    }
}
