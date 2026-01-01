package com.angl.presentation

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.angl.presentation.camera.CameraScreen
import com.angl.presentation.permission.PermissionWrapper
import com.angl.presentation.theme.AnglTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for the Angl application.
 * 
 * This activity serves as the entry point and hosts the camera screen with:
 * - Immersive full-screen experience (hides system bars)
 * - Keep screen on during camera usage
 * - Permission handling wrapper
 * - Hilt dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on during camera usage
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Hide system bars for immersive full-screen experience
        setupImmersiveMode()
        
        setContent {
            AnglTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // PermissionWrapper handles camera permission flow
                    PermissionWrapper {
                        CameraScreen()
                    }
                }
            }
        }
    }
    
    /**
     * Configures immersive sticky mode for full-screen experience.
     * Hides both status bar and navigation bar.
     * Bars reappear temporarily on swipe and hide automatically.
     */
    private fun setupImmersiveMode() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Modern approach for API 30+
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Legacy approach for older APIs
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Reapply immersive mode when returning to the activity
        setupImmersiveMode()
    }
}
