package com.angl.presentation.viewmodel

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angl.domain.model.CameraError
import com.angl.domain.model.CameraResult
import com.angl.domain.repository.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for camera operations.
 * 
 * This ViewModel follows MVVM architecture and manages the camera state.
 * It provides a clean separation between UI and business logic.
 * 
 * State is exposed via StateFlow for reactive UI updates in Compose.
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository
) : ViewModel() {

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initial)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    /**
     * Starts the camera with the given preview view and lifecycle owner.
     * Updates the camera state based on the result.
     * 
     * @param previewView The view to display camera preview
     * @param lifecycleOwner The lifecycle owner to bind camera lifecycle
     */
    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            _cameraState.value = CameraState.Loading
            
            when (val result = cameraRepository.startCamera(previewView, lifecycleOwner)) {
                is CameraResult.Success -> {
                    _cameraState.value = CameraState.Success
                }
                is CameraResult.Error -> {
                    _cameraState.value = CameraState.Error(
                        error = result.exception as? CameraError ?: CameraError.Unknown(),
                        message = result.message
                    )
                }
            }
        }
    }

    /**
     * Stops the camera and releases resources.
     */
    fun stopCamera() {
        cameraRepository.stopCamera()
        _cameraState.value = CameraState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        cameraRepository.stopCamera()
    }
}

/**
 * Sealed class representing different camera states.
 * This ensures type-safe state management and exhaustive when expressions.
 */
sealed class CameraState {
    data object Initial : CameraState()
    data object Loading : CameraState()
    data object Success : CameraState()
    data class Error(val error: CameraError, val message: String) : CameraState()
}
