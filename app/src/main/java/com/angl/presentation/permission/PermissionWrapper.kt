package com.angl.presentation.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * PermissionWrapper handles camera permission flow with beautiful UX.
 * 
 * This composable:
 * - Checks camera permission status on launch
 * - Shows camera UI if permission granted (happy path)
 * - Shows permission rationale if denied (sad path)
 * - Handles "Don't ask again" scenario by directing to settings
 * - Provides professional, user-friendly messaging
 * 
 * @param content The content to show when permission is granted (typically CameraScreen)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionWrapper(
    content: @Composable () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            // Happy path: Permission granted, show camera
            content()
        }
        else -> {
            // Sad path: Permission denied or not yet requested
            PermissionDeniedScreen(
                shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }
    }
}

/**
 * Beautiful permission rationale screen shown when camera access is denied.
 */
@Composable
private fun PermissionDeniedScreen(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Camera icon
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Headline
        Text(
            text = "We Need Your Eyes",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Explanation
        Text(
            text = if (shouldShowRationale) {
                "Angl uses your camera to analyze your photos in real-time and guide you to take the perfect shot. " +
                "We never save or upload your images."
            } else {
                "Angl needs camera access to help you take better photos. " +
                "Camera permission has been permanently denied. " +
                "Please enable it in your device settings."
            },
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Action button
        Button(
            onClick = {
                if (shouldShowRationale) {
                    onRequestPermission()
                } else {
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (shouldShowRationale) "Grant Camera Access" else "Open Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (shouldShowRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Removed "Maybe Later" button as camera permission is required
            // User can exit the app or use back button if they don't want to grant permission
        }
    }
}
