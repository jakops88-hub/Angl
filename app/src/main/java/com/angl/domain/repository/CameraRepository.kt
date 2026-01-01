package com.angl.domain.repository

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.angl.domain.model.CameraResult

/**
 * Repository interface for camera operations.
 * Following Clean Architecture principles, this interface defines the contract
 * for camera operations without exposing implementation details.
 */
interface CameraRepository {
    /**
     * Starts the camera preview with performance-optimized settings.
     * 
     * @param previewView The view to display camera preview
     * @param lifecycleOwner The lifecycle owner to bind camera lifecycle
     * @return CameraResult indicating success or specific error
     */
    suspend fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ): CameraResult<Unit>
    
    /**
     * Stops the camera and releases resources.
     */
    fun stopCamera()
}
