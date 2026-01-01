package com.angl.domain.model

/**
 * Represents different camera errors that can occur.
 * These errors help in providing specific user feedback.
 */
sealed class CameraError : Throwable() {
    data object PermissionDenied : CameraError() {
        override val message: String = "Camera permission is required"
    }
    
    data object CameraNotAvailable : CameraError() {
        override val message: String = "Camera is not available on this device"
    }
    
    data class InitializationFailed(override val message: String) : CameraError()
    
    data class Unknown(override val message: String = "Unknown camera error occurred") : CameraError()
}
