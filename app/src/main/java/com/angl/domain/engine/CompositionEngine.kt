package com.angl.domain.engine

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

/**
 * CompositionEngine is the brain of Angl.
 * 
 * This is a pure Kotlin class with no Android dependencies.
 * It analyzes pose data and provides real-time feedback for photo composition.
 * 
 * Key responsibilities:
 * - Analyze shoulder alignment (horizon/level detection)
 * - Calculate headroom (subject framing)
 * - Determine composition quality
 * - Provide actionable feedback for better photos
 */
class CompositionEngine {

    companion object {
        // Thresholds for composition analysis
        private const val SHOULDER_ANGLE_THRESHOLD_DEGREES = 5.0
        private const val HEADROOM_MIN_PERCENTAGE = 0.10 // 10% of screen height
        private const val SHOULDER_ANGLE_PERFECT_DEGREES = 2.0
    }

    /**
     * Analyzes a detected pose and returns composition feedback.
     * 
     * This function evaluates:
     * 1. Shoulder alignment (is the horizon level?)
     * 2. Headroom (is there space above the head?)
     * 
     * @param pose The detected pose from ML Kit
     * @param frameHeight Height of the camera frame (for headroom calculation)
     * @return FeedbackState indicating composition quality and guidance
     */
    fun analyzePose(pose: Pose, frameHeight: Float = 1.0f): FeedbackState {
        // Get key landmarks
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

        // Check if we have the required landmarks
        if (leftShoulder == null || rightShoulder == null) {
            return FeedbackState.Warning("POSITION YOURSELF IN FRAME")
        }

        // 1. Check shoulder alignment (horizon level)
        val shoulderAngle = calculateShoulderAngle(leftShoulder, rightShoulder)
        val shoulderAngleDegrees = shoulderAngle * 180.0 / PI
        
        if (abs(shoulderAngleDegrees) > SHOULDER_ANGLE_THRESHOLD_DEGREES) {
            val direction = if (shoulderAngleDegrees > 0) "LEFT" else "RIGHT"
            return FeedbackState.Critical("TILT PHONE $direction")
        }

        // 2. Check headroom (distance from top of head to top of frame)
        if (nose != null) {
            val headroom = calculateHeadroom(nose, frameHeight)
            
            if (headroom < HEADROOM_MIN_PERCENTAGE) {
                return FeedbackState.Warning("TILT UP FOR MORE HEADROOM")
            }
        }

        // 3. Check if composition is perfect
        if (abs(shoulderAngleDegrees) <= SHOULDER_ANGLE_PERFECT_DEGREES) {
            return FeedbackState.Perfect
        }

        // Minor adjustments needed
        return FeedbackState.Warning("SLIGHT ADJUSTMENT NEEDED")
    }

    /**
     * Calculates the angle between two shoulder landmarks.
     * 
     * A horizontal line has angle = 0.
     * Positive angle means left shoulder is higher.
     * Negative angle means right shoulder is higher.
     * 
     * @param leftShoulder Left shoulder landmark
     * @param rightShoulder Right shoulder landmark
     * @return Angle in radians
     */
    private fun calculateShoulderAngle(
        leftShoulder: PoseLandmark,
        rightShoulder: PoseLandmark
    ): Double {
        val leftPos = leftShoulder.position
        val rightPos = rightShoulder.position
        
        // Calculate angle using atan2
        // atan2(dy, dx) gives angle from horizontal
        val dx = (rightPos.x - leftPos.x).toDouble()
        val dy = (rightPos.y - leftPos.y).toDouble()
        
        return atan2(dy, dx)
    }

    /**
     * Calculates headroom as a percentage of frame height.
     * 
     * Headroom is the space between the top of the head (nose landmark)
     * and the top of the frame. Good composition typically has 10-20% headroom.
     * 
     * Note: In camera coordinates, Y=0 is at the top of the frame.
     * A smaller noseY means the head is closer to the top (less headroom).
     * 
     * @param nose Nose landmark (approximates top of head)
     * @param frameHeight Height of the camera frame
     * @return Headroom as a percentage (0.0 to 1.0)
     */
    private fun calculateHeadroom(nose: PoseLandmark, frameHeight: Float): Float {
        val noseY = nose.position.y
        
        // Headroom is the distance from top of frame (Y=0) to nose
        // Normalize by frame height to get percentage
        // noseY directly represents the space from top, so we just normalize it
        val headroom = noseY / frameHeight
        
        return headroom
    }

    /**
     * Analyzes pose with additional guidance types for more comprehensive feedback.
     * 
     * This extended version can detect more composition issues:
     * - Subject too far/close
     * - Off-center composition
     * - Movement/stability issues
     * 
     * @param pose The detected pose
     * @param frameWidth Width of the camera frame
     * @param frameHeight Height of the camera frame
     * @return Enhanced FeedbackState with direction guidance
     */
    fun analyzePoseExtended(
        pose: Pose,
        frameWidth: Float,
        frameHeight: Float
    ): FeedbackStateExtended {
        // Get key landmarks
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (leftShoulder == null || rightShoulder == null) {
            return FeedbackStateExtended.Warning(
                message = "POSITION YOURSELF IN FRAME",
                guidanceType = GuidanceType.None
            )
        }

        // Check shoulder alignment
        val shoulderAngle = calculateShoulderAngle(leftShoulder, rightShoulder)
        val shoulderAngleDegrees = shoulderAngle * 180.0 / PI
        
        if (abs(shoulderAngleDegrees) > SHOULDER_ANGLE_THRESHOLD_DEGREES) {
            val rotationDirection = if (shoulderAngleDegrees > 0) 
                RotationDirection.TILT_LEFT 
            else 
                RotationDirection.TILT_RIGHT
            
            return FeedbackStateExtended.Critical(
                message = "TILT PHONE ${if (shoulderAngleDegrees > 0) "LEFT" else "RIGHT"}",
                guidanceType = GuidanceType.Rotation(rotationDirection)
            )
        }

        // Check headroom
        if (nose != null) {
            val headroom = calculateHeadroom(nose, frameHeight)
            
            if (headroom < HEADROOM_MIN_PERCENTAGE) {
                return FeedbackStateExtended.Warning(
                    message = "TILT UP FOR MORE HEADROOM",
                    guidanceType = GuidanceType.Translation(TranslationDirection.UP)
                )
            }
        }

        // Check horizontal centering
        if (nose != null) {
            val centerX = frameWidth / 2
            val noseX = nose.position.x
            val offsetRatio = abs(noseX - centerX) / frameWidth
            
            if (offsetRatio > 0.2) { // More than 20% off-center
                val direction = if (noseX < centerX) 
                    TranslationDirection.RIGHT 
                else 
                    TranslationDirection.LEFT
                
                return FeedbackStateExtended.Warning(
                    message = "MOVE ${direction.name}",
                    guidanceType = GuidanceType.Translation(direction)
                )
            }
        }

        // Check subject distance (using shoulder width as proxy)
        if (leftShoulder != null && rightShoulder != null) {
            val shoulderWidth = abs(rightShoulder.position.x - leftShoulder.position.x)
            val shoulderWidthRatio = shoulderWidth / frameWidth
            
            when {
                shoulderWidthRatio < 0.15 -> {
                    return FeedbackStateExtended.Warning(
                        message = "MOVE CLOSER",
                        guidanceType = GuidanceType.Translation(TranslationDirection.FORWARD)
                    )
                }
                shoulderWidthRatio > 0.6 -> {
                    return FeedbackStateExtended.Warning(
                        message = "MOVE BACK",
                        guidanceType = GuidanceType.Translation(TranslationDirection.BACK)
                    )
                }
            }
        }

        // Perfect composition
        if (abs(shoulderAngleDegrees) <= SHOULDER_ANGLE_PERFECT_DEGREES) {
            return FeedbackStateExtended.Perfect
        }

        return FeedbackStateExtended.Warning(
            message = "SLIGHT ADJUSTMENT NEEDED",
            guidanceType = GuidanceType.None
        )
    }
}

/**
 * Sealed class representing composition feedback states.
 * 
 * Three levels of feedback:
 * - Perfect: Composition is excellent, ready to shoot
 * - Warning: Minor adjustments recommended
 * - Critical: Major issues that need immediate correction
 */
sealed class FeedbackState {
    data object Perfect : FeedbackState()
    data class Warning(val message: String) : FeedbackState()
    data class Critical(val message: String) : FeedbackState()
}

/**
 * Extended feedback state with guidance type for AR overlay.
 */
sealed class FeedbackStateExtended {
    data object Perfect : FeedbackStateExtended()
    data class Warning(
        val message: String,
        val guidanceType: GuidanceType
    ) : FeedbackStateExtended()
    data class Critical(
        val message: String,
        val guidanceType: GuidanceType
    ) : FeedbackStateExtended()
}

/**
 * Types of visual guidance for AR overlay.
 */
sealed class GuidanceType {
    data object None : GuidanceType()
    data class Translation(val direction: TranslationDirection) : GuidanceType()
    data class Rotation(val direction: RotationDirection) : GuidanceType()
}

/**
 * Directions for translation guidance (move left/right/up/down/forward/back).
 */
enum class TranslationDirection {
    LEFT, RIGHT, UP, DOWN, FORWARD, BACK
}

/**
 * Directions for rotation guidance (tilt left/right).
 */
enum class RotationDirection {
    TILT_LEFT, TILT_RIGHT
}
