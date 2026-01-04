package com.angl.domain.engine

import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for CompositionEngine.
 * 
 * These tests verify that the composition analysis logic works correctly
 * for various pose scenarios without requiring an actual camera or ML Kit runtime.
 */
class CompositionEngineTest {

    private lateinit var compositionEngine: CompositionEngine

    @Before
    fun setup() {
        compositionEngine = CompositionEngine()
    }

    /**
     * Test: Perfect pose with level shoulders and good headroom.
     * Expected: FeedbackState.Perfect
     */
    @Test
    fun testPerfectPose() {
        // Create a mock pose with perfectly level shoulders (0 degrees)
        val pose = createMockPose(
            leftShoulderX = 200f,
            leftShoulderY = 300f,
            rightShoulderX = 400f,
            rightShoulderY = 300f,  // Same Y = level shoulders
            noseX = 300f,
            noseY = 150f  // Good headroom (150 / 1000 = 15%)
        )

        val result = compositionEngine.analyzePose(pose, frameHeight = 1000f)

        assertTrue("Expected Perfect state for level shoulders and good headroom", 
            result is FeedbackState.Perfect)
    }

    /**
     * Test: Critical tilt with shoulders tilted more than 5 degrees.
     * Expected: FeedbackState.Critical with tilt instruction
     */
    @Test
    fun testCriticalTilt() {
        // Create a mock pose with significant tilt (>5 degrees)
        // With 200px horizontal distance and 40px vertical difference:
        // angle = atan(40/200) ≈ 11.3 degrees
        val pose = createMockPose(
            leftShoulderX = 200f,
            leftShoulderY = 280f,  // Left shoulder higher
            rightShoulderX = 400f,
            rightShoulderY = 320f,  // Right shoulder lower (40px difference gives ~11 degrees)
            noseX = 300f,
            noseY = 150f
        )

        val result = compositionEngine.analyzePose(pose, frameHeight = 1000f)

        assertTrue("Expected Critical state for tilted shoulders", 
            result is FeedbackState.Critical)
        
        if (result is FeedbackState.Critical) {
            assertTrue("Expected tilt message in critical feedback",
                result.message.contains("TILT"))
        }
    }

    /**
     * Test: Warning for insufficient headroom (subject too close to top edge).
     * Expected: FeedbackState.Warning with headroom instruction
     */
    @Test
    fun testWarningHeadroom() {
        // Create a mock pose with level shoulders but insufficient headroom
        val pose = createMockPose(
            leftShoulderX = 200f,
            leftShoulderY = 300f,
            rightShoulderX = 400f,
            rightShoulderY = 300f,  // Level shoulders
            noseX = 300f,
            noseY = 50f  // Too close to top (50 / 1000 = 5% < 10% minimum)
        )

        val result = compositionEngine.analyzePose(pose, frameHeight = 1000f)

        assertTrue("Expected Warning state for insufficient headroom", 
            result is FeedbackState.Warning)
        
        if (result is FeedbackState.Warning) {
            assertTrue("Expected headroom message in warning feedback",
                result.message.contains("HEADROOM"))
        }
    }

    /**
     * Helper function to create a mock Pose with specified landmarks.
     * This allows us to test the CompositionEngine without ML Kit runtime.
     */
    private fun createMockPose(
        leftShoulderX: Float,
        leftShoulderY: Float,
        rightShoulderX: Float,
        rightShoulderY: Float,
        noseX: Float,
        noseY: Float
    ): Pose {
        val mockPose = mock(Pose::class.java)
        
        // Create mock landmarks
        val leftShoulder = createMockLandmark(leftShoulderX, leftShoulderY)
        val rightShoulder = createMockLandmark(rightShoulderX, rightShoulderY)
        val nose = createMockLandmark(noseX, noseY)
        
        // Configure the mock pose to return our landmarks
        `when`(mockPose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)).thenReturn(leftShoulder)
        `when`(mockPose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)).thenReturn(rightShoulder)
        `when`(mockPose.getPoseLandmark(PoseLandmark.NOSE)).thenReturn(nose)
        
        return mockPose
    }

    /**
     * Helper function to create a mock PoseLandmark at a specific position.
     */
    private fun createMockLandmark(x: Float, y: Float): PoseLandmark {
        val mockLandmark = mock(PoseLandmark::class.java)
        val position = PointF3D.from(x, y, 0f)
        
        `when`(mockLandmark.position).thenReturn(position)
        
        return mockLandmark
    }
}
