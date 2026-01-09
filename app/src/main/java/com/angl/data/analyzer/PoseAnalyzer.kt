package com.angl.data.analyzer

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PoseAnalyzer implements ImageAnalysis.Analyzer to process camera frames
 * and detect human poses using Google ML Kit's Pose Detection API.
 * 
 * This analyzer:
 * - Uses the Accurate model for better landmark detection
 * - Handles image rotation correctly for all device orientations
 * - Emits detected poses via StateFlow for ViewModel observation
 * - Closes ImageProxy immediately to maintain 60fps processing
 * 
 * The Accurate model is prioritized for quality, but the implementation
 * ensures frames are processed quickly to maintain target FPS.
 */
@Singleton
class PoseAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {

    private val _poseFlow = MutableStateFlow<Pose?>(null)
    val poseFlow: StateFlow<Pose?> = _poseFlow.asStateFlow()

    // Use AtomicBoolean for thread-safe processing flag
    private val isProcessingFlag = AtomicBoolean(false)
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /**
     * Pose detector configured with Accurate model.
     * 
     * AccuratePoseDetectorOptions provides:
     * - Better landmark accuracy
     * - More stable tracking
     * - Confidence scores for each landmark
     * 
     * The detector is initialized lazily to avoid startup overhead.
     */
    private val poseDetector: PoseDetector by lazy {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        
        PoseDetection.getClient(options)
    }

    companion object {
        private const val TAG = "PoseAnalyzer"
    }

    /**
     * Analyzes each camera frame for pose detection.
     * 
     * Critical implementation details:
     * 1. Converts ImageProxy to InputImage with correct rotation
     * 2. Processes image with ML Kit Pose Detection
     * 3. Emits detected pose via StateFlow
     * 4. ALWAYS closes ImageProxy to avoid frame drops
     * 
     * The rotation degrees from ImageProxy.imageInfo.rotationDegrees
     * ensures the image is correctly oriented regardless of device rotation.
     * 
     * @param imageProxy The camera frame to analyze
     */
    override fun analyze(imageProxy: ImageProxy) {
        // Atomic check-and-set to prevent race conditions
        if (!isProcessingFlag.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        _isProcessing.value = true

        try {
            // Convert ImageProxy to InputImage with correct rotation
            // rotationDegrees handles device orientation automatically
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                Log.w(TAG, "Image is null, skipping frame")
                resetProcessingState(imageProxy)
                return
            }

            // Create InputImage with rotation degrees from imageProxy
            // This is CRITICAL - rotation must match device orientation
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            // Process image with pose detector
            poseDetector.process(inputImage)
                .addOnSuccessListener { pose ->
                    // Emit detected pose via StateFlow
                    // ViewModel can observe this for UI updates
                    _poseFlow.value = pose
                    
                    if (pose.allPoseLandmarks.isNotEmpty()) {
                        Log.d(TAG, "Pose detected with ${pose.allPoseLandmarks.size} landmarks")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Pose detection failed", exception)
                    // Emit null to indicate no pose detected
                    _poseFlow.value = null
                }
                .addOnCompleteListener {
                    // CRITICAL: Close ImageProxy immediately after processing
                    // This releases the frame and allows the next one to be processed
                    // Without this, frames will be dropped and FPS will suffer
                    resetProcessingState(imageProxy)
                }
        } catch (e: Throwable) {
            Log.e(TAG, "Error analyzing image", e)
            // Always close ImageProxy even on error
            resetProcessingState(imageProxy)
        }
    }

    /**
     * Resets processing state and releases the current frame.
     * Ensures proper cleanup in all exit paths.
     */
    private fun resetProcessingState(imageProxy: ImageProxy) {
        imageProxy.close()
        _isProcessing.value = false
        isProcessingFlag.set(false)
    }

    /**
     * Cleanup method to release ML Kit resources.
     * Should be called when analyzer is no longer needed.
     */
    fun close() {
        try {
            poseDetector.close()
            Log.d(TAG, "PoseAnalyzer closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing PoseAnalyzer", e)
        }
    }
}
