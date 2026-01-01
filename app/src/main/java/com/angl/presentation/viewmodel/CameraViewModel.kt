package com.angl.presentation.viewmodel

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angl.data.analyzer.PoseAnalyzer
import com.angl.domain.engine.CompositionEngine
import com.angl.domain.engine.FeedbackStateExtended
import com.angl.domain.engine.GuidanceType
import com.angl.domain.model.CameraError
import com.angl.domain.model.CameraResult
import com.angl.domain.repository.CameraRepository
import com.angl.util.feedback.SoundHelper
import com.angl.util.feedback.VibrationHelper
import com.google.mlkit.vision.pose.Pose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for camera operations.
 * 
 * This ViewModel follows MVVM architecture and manages the camera state.
 * It provides a clean separation between UI and business logic.
 * 
 * State is exposed via StateFlow for reactive UI updates in Compose.
 * Pose detection results are also exposed via StateFlow from PoseAnalyzer.
 * Composition feedback is provided by CompositionEngine analysis.
 * Haptic and audio feedback enhance the user experience.
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val poseAnalyzer: PoseAnalyzer,
    private val compositionEngine: CompositionEngine,
    private val vibrationHelper: VibrationHelper,
    private val soundHelper: SoundHelper
) : ViewModel() {

    companion object {
        // Camera frame dimensions - matches ImageAnalysis resolution in CameraManager
        private const val CAMERA_FRAME_WIDTH = 640f
        private const val CAMERA_FRAME_HEIGHT = 480f
    }

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initial)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    /**
     * Expose pose detection results from PoseAnalyzer.
     * UI can observe this to render pose overlays.
     */
    val detectedPose: StateFlow<Pose?> = poseAnalyzer.poseFlow

    /**
     * Expose processing state from PoseAnalyzer.
     * UI can use this to show processing indicators.
     */
    val isAnalyzing: StateFlow<Boolean> = poseAnalyzer.isProcessing

    /**
     * Composition feedback state derived from detected pose.
     * This is the "brain" output that drives the AR overlay.
     */
    val feedbackState: StateFlow<FeedbackStateExtended> = detectedPose
        .map { pose ->
            if (pose != null) {
                // Analyze pose with composition engine
                compositionEngine.analyzePoseExtended(
                    pose = pose,
                    frameWidth = CAMERA_FRAME_WIDTH,
                    frameHeight = CAMERA_FRAME_HEIGHT
                )
            } else {
                // No pose detected
                FeedbackStateExtended.Warning(
                    message = "POSITION YOURSELF IN FRAME",
                    guidanceType = GuidanceType.None
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeedbackStateExtended.Warning(
                message = "INITIALIZING...",
                guidanceType = GuidanceType.None
            )
        )

    init {
        // Observe feedback state transitions and trigger sensory feedback
        viewModelScope.launch {
            var previousState: FeedbackStateExtended? = null
            
            feedbackState.collect { currentState ->
                // Trigger haptic/audio feedback on state transitions
                when {
                    // Perfect state reached - trigger success feedback
                    previousState !is FeedbackStateExtended.Perfect && 
                    currentState is FeedbackStateExtended.Perfect -> {
                        vibrationHelper.vibrateConfirm()
                        soundHelper.playSuccessSound()
                    }
                    // Critical state - trigger warning haptic
                    previousState !is FeedbackStateExtended.Critical &&
                    currentState is FeedbackStateExtended.Critical -> {
                        vibrationHelper.vibrateCritical()
                    }
                }
                
                previousState = currentState
            }
        }
    }

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

    /**
     * Resets the camera state to Initial.
     * This allows retry by triggering recomposition with initial state.
     */
    fun resetState() {
        _cameraState.value = CameraState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        cameraRepository.stopCamera()
        // Note: soundHelper.release() not called as it's a singleton
        // and should persist for the app lifetime
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
