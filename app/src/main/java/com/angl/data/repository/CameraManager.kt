package com.angl.data.repository

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.angl.domain.model.CameraError
import com.angl.domain.model.CameraResult
import com.angl.domain.repository.CameraRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * CameraManager wraps the CameraX API and handles camera lifecycle management.
 * 
 * This class is designed for Jetpack Compose environments and follows MVVM 
 * and Clean Architecture principles. It is optimized for real-time performance
 * with low-latency configuration.
 * 
 * Key features:
 * - Null-safe implementation
 * - Memory-leak prevention through proper lifecycle management
 * - Performance mode configuration for 60fps real-time processing
 * - Comprehensive error handling for permissions and hardware availability
 * 
 * @param context Application context for accessing system services
 */
@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val poseAnalyzer: com.angl.data.analyzer.PoseAnalyzer
) : CameraRepository {

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    
    /**
     * Executor for camera operations to ensure they run on a background thread.
     * This prevents blocking the main thread and ensures smooth UI performance.
     */
    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    companion object {
        private const val TAG = "CameraManager"
    }

    /**
     * Starts the camera with performance-optimized settings.
     * 
     * This function:
     * 1. Checks camera permissions
     * 2. Verifies camera hardware availability
     * 3. Configures camera for low-latency performance mode
     * 4. Binds camera lifecycle to the provided LifecycleOwner
     * 
     * The camera is configured with:
     * - Back camera as default
     * - Preview use case for real-time display
     * - ImageAnalysis use case for ML Kit processing
     * - STRATEGY_KEEP_ONLY_LATEST for low latency (drops frames if processing is slow)
     * - 640x480 resolution for optimal performance vs quality balance
     * 
     * @param previewView The PreviewView to display camera feed
     * @param lifecycleOwner The LifecycleOwner (typically Activity or Fragment) to bind camera lifecycle
     * @return CameraResult<Unit> indicating success or specific error
     */
    override suspend fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ): CameraResult<Unit> = withContext(Dispatchers.Main) {
        try {
            // Check camera permission
            if (!hasPermission()) {
                Log.e(TAG, "Camera permission not granted")
                return@withContext CameraResult.Error(
                    CameraError.PermissionDenied,
                    CameraError.PermissionDenied.message ?: "Permission denied"
                )
            }

            // Check camera hardware availability
            if (!hasCameraHardware()) {
                Log.e(TAG, "Camera hardware not available")
                return@withContext CameraResult.Error(
                    CameraError.CameraNotAvailable,
                    CameraError.CameraNotAvailable.message ?: "Camera not available"
                )
            }

            // Get camera provider instance
            val provider = getCameraProvider()
            cameraProvider = provider

            // Unbind all use cases before rebinding
            provider.unbindAll()

            // Configure camera selector (back camera)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // Configure preview use case
            // This displays the camera feed in real-time
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Configure image analysis use case for ML Kit
            // STRATEGY_KEEP_ONLY_LATEST ensures low latency by dropping frames
            // if processing cannot keep up (critical for 60fps performance)
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                // 640x480 provides good balance between performance and quality
                // Lower resolution = faster ML processing
                .setTargetResolution(android.util.Size(640, 480))
                .build()

            // Attach PoseAnalyzer to process frames for pose detection
            // The analyzer runs on the camera executor thread for optimal performance
            imageAnalysis.setAnalyzer(cameraExecutor, poseAnalyzer)

            // Configure image capture use case for taking photos
            // CAPTURE_MODE_MAXIMIZE_QUALITY ensures high-quality photos
            val imageCaptureUseCase = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setJpegQuality(95) // High quality for professional photos
                .build()
            
            imageCapture = imageCaptureUseCase

            try {
                // Bind use cases to camera lifecycle
                // This ensures camera resources are properly managed according to lifecycle
                // NOW INCLUDES ImageCapture for photo taking!
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis,
                    imageCaptureUseCase  // CRITICAL: Enables photo capture
                )

                Log.d(TAG, "Camera started successfully")
                CameraResult.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera use cases", e)
                CameraResult.Error(
                    CameraError.InitializationFailed(e.message ?: "Camera binding failed"),
                    e.message ?: "Failed to bind camera"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Camera initialization failed", e)
            CameraResult.Error(
                CameraError.Unknown(e.message ?: "Unknown error"),
                e.message ?: "Camera initialization failed"
            )
        }
    }

    /**
     * Stops the camera and releases resources.
     * Ensures no memory leaks by properly unbinding all use cases.
     */
    override fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            camera = null
            imageCapture = null
            Log.d(TAG, "Camera stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
        }
    }

    /**
     * Takes a photo with the current camera configuration.
     * 
     * Captures a high-quality JPEG image and saves it to the device's MediaStore (Gallery).
     * The photo will appear in the device's Gallery app with proper metadata.
     * 
     * @param onPhotoCaptured Callback with the URI string of the captured photo
     * @param onError Callback with error message if capture fails
     */
    fun takePhoto(
        onPhotoCaptured: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = imageCapture ?: run {
            Log.e(TAG, "ImageCapture not initialized")
            onError("Camera not ready for photo capture")
            return
        }

        // Create ContentValues with metadata for MediaStore
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val fileName = "ANGL_$timeStamp"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            
            // For Android 10 (Q) and above, use MediaStore with relative path
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Angl")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        // Configure output options to save to MediaStore
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        // Capture photo
        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    
                    // For Android 10+, mark the file as ready (not pending)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && savedUri != null) {
                        val updateValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                        }
                        context.contentResolver.update(savedUri, updateValues, null, null)
                    }
                    
                    Log.d(TAG, "Photo saved to Gallery: $savedUri")
                    onPhotoCaptured(savedUri?.toString() ?: "")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exception)
                    onError("Failed to capture photo: ${exception.message}")
                }
            }
        )
    }

    /**
     * Checks if camera permission is granted.
     * 
     * @return true if camera permission is granted, false otherwise
     */
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the device has camera hardware.
     * 
     * @return true if camera hardware is available, false otherwise
     */
    private fun hasCameraHardware(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    /**
     * Gets the ProcessCameraProvider instance.
     * Uses suspendCoroutine to convert the ListenableFuture to a suspend function.
     * 
     * @return ProcessCameraProvider instance
     * @throws Exception if provider cannot be obtained
     */
    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                continuation.resume(cameraProviderFuture.get())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
                continuation.resumeWith(Result.failure(e))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Cleanup method to release resources when no longer needed.
     * Should be called when the application is being destroyed.
     */
    fun cleanup() {
        stopCamera()
        poseAnalyzer.close()
        cameraExecutor.shutdown()
        Log.d(TAG, "Camera resources cleaned up")
    }
}
