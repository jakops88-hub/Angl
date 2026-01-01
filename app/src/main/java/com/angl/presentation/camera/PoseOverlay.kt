package com.angl.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.angl.util.CoordinateMapper
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * PoseOverlay composable draws pose landmarks and skeleton over the camera preview.
 * 
 * This demonstrates the critical coordinate mapping between camera space
 * and screen space. Without proper mapping, landmarks would appear misaligned.
 * 
 * @param pose The detected pose from ML Kit
 * @param sourceSize Size of the camera image (e.g., 640x480)
 * @param isMirrored Whether using front camera (requires mirroring)
 * @param modifier Modifier for the canvas
 */
@Composable
fun PoseOverlay(
    pose: Pose?,
    sourceSize: IntSize = IntSize(640, 480),
    isMirrored: Boolean = false,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    // Convert dp to pixels properly using density
    val targetSize = with(density) {
        IntSize(
            configuration.screenWidthDp.dp.toPx().toInt(),
            configuration.screenHeightDp.dp.toPx().toInt()
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        pose?.let { detectedPose ->
            // Get all landmarks
            val landmarks = detectedPose.allPoseLandmarks
            
            if (landmarks.isEmpty()) return@Canvas
            
            // Map landmarks from camera space to screen space
            val mappedLandmarks = landmarks.mapNotNull { landmark ->
                val position = landmark.position
                val screenPos = CoordinateMapper.mapPoint(
                    x = position.x,
                    y = position.y,
                    sourceSize = sourceSize,
                    targetSize = targetSize,
                    isMirrored = isMirrored
                )
                landmark.landmarkType to screenPos
            }.toMap()
            
            // Draw landmarks as circles
            mappedLandmarks.values.forEach { position ->
                drawCircle(
                    color = Color.Green,
                    radius = 8f,
                    center = position,
                    style = Stroke(width = 3f)
                )
            }
            
            // Draw skeleton connections
            drawSkeletonConnections(mappedLandmarks)
        }
    }
}

/**
 * Draws lines connecting pose landmarks to form a skeleton.
 * 
 * This follows the standard pose landmark connections for a human body.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSkeletonConnections(
    landmarks: Map<Int, Offset>
) {
    // Define landmark connections (standard pose skeleton)
    val connections = listOf(
        // Face
        PoseLandmark.LEFT_EAR to PoseLandmark.LEFT_EYE,
        PoseLandmark.LEFT_EYE to PoseLandmark.NOSE,
        PoseLandmark.NOSE to PoseLandmark.RIGHT_EYE,
        PoseLandmark.RIGHT_EYE to PoseLandmark.RIGHT_EAR,
        
        // Upper body
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
        PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
        
        // Torso
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
        PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
        
        // Lower body
        PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
        PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
        PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE
    )
    
    // Draw lines for each connection
    connections.forEach { (start, end) ->
        val startPos = landmarks[start]
        val endPos = landmarks[end]
        
        if (startPos != null && endPos != null) {
            drawLine(
                color = Color.Cyan,
                start = startPos,
                end = endPos,
                strokeWidth = 4f
            )
        }
    }
}
