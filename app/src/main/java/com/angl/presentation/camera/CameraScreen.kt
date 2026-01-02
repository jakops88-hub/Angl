package com.angl.presentation.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.angl.R
import com.angl.domain.model.CameraError
import com.angl.presentation.viewmodel.CameraState
import com.angl.presentation.viewmodel.CameraViewModel

/**
 * Main camera screen composable.
 * 
 * This composable:
 * 1. Handles camera permission requests
 * 2. Displays the camera preview
 * 3. Shows appropriate UI based on camera state
 * 4. Manages camera lifecycle through ViewModel
 * 5. Displays pose detection overlay with coordinate mapping
 * 
 * The implementation is optimized for Jetpack Compose and follows Material3 design.
 */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraState by viewModel.cameraState.collectAsState()
    val detectedPose by viewModel.detectedPose.collectAsState()
    val feedbackState by viewModel.feedbackState.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    
    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Check permission on first composition
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    // Show snackbar when photo is captured
    LaunchedEffect(photoUri) {
        photoUri?.let { uri ->
            snackbarHostState.showSnackbar(
                message = "Photo saved: $uri",
                duration = SnackbarDuration.Short
            )
            viewModel.clearPhotoUri()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
        when {
            !hasCameraPermission -> {
                PermissionRequiredContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
            else -> {
                when (cameraState) {
                    is CameraState.Initial,
                    is CameraState.Loading -> {
                        CameraPreviewContent(
                            onPreviewViewCreated = { previewView ->
                                viewModel.startCamera(previewView, lifecycleOwner)
                            }
                        )
                        if (cameraState is CameraState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    is CameraState.Success -> {
                        // Camera preview
                        CameraPreviewContent(
                            onPreviewViewCreated = { /* Already started */ }
                        )
                        
                        // Professional AR overlay with composition guidance
                        AnglOverlay(
                            feedbackState = feedbackState
                        )
                        
                        // Premium shutter button at bottom center
                        ShutterButton(
                            onClick = { viewModel.takePhoto() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 48.dp)
                        )
                        
                        // Optional: Keep pose skeleton overlay for debugging
                        // PoseOverlay(
                        //     pose = detectedPose,
                        //     isMirrored = false
                        // )
                    }
                    is CameraState.Error -> {
                        val error = (cameraState as CameraState.Error)
                        ErrorContent(
                            error = error.error,
                            message = error.message,
                            onRetry = {
                                // Reset state to Initial to trigger camera preview recreation
                                viewModel.resetState()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable that displays the camera preview using AndroidView.
 * 
 * PreviewView is a legacy Android View, so we use AndroidView to integrate it
 * with Compose. The view is created once and reused for lifecycle management.
 * 
 * @param onPreviewViewCreated Callback invoked when PreviewView is created
 */
@Composable
private fun CameraPreviewContent(
    onPreviewViewCreated: (PreviewView) -> Unit
) {
    val context = LocalContext.current
    
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                // Set scale type to fill the view while maintaining aspect ratio
                scaleType = PreviewView.ScaleType.FILL_CENTER
                // Enable hardware acceleration for better performance
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                onPreviewViewCreated(this)
            }
        }
    )
}

/**
 * Composable that displays permission required message.
 * 
 * @param onRequestPermission Callback to request camera permission
 */
@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.camera_permission_required),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text(text = stringResource(R.string.grant_permission))
        }
    }
}

/**
 * Composable that displays error state.
 * 
 * Shows specific error messages and provides retry functionality.
 * 
 * @param error The camera error that occurred
 * @param message Error message to display
 * @param onRetry Callback to retry camera initialization
 */
@Composable
private fun ErrorContent(
    error: CameraError,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (error) {
                is CameraError.PermissionDenied -> stringResource(R.string.camera_permission_required)
                is CameraError.CameraNotAvailable -> stringResource(R.string.camera_not_available)
                is CameraError.InitializationFailed,
                is CameraError.Unknown -> stringResource(R.string.camera_error)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (error !is CameraError.PermissionDenied && error !is CameraError.CameraNotAvailable) {
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}
