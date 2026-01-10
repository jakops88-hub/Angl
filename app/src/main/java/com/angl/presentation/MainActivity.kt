package com.angl.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        // Use WindowCompat which safely handles the DecorView creation
        // and provides backward compatibility automatically.
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)

        // Configure the behavior
        windowInsetsController.systemBarsBehavior = 
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide system bars (status bar and navigation bar)
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
    }
    
    override fun onResume() {
        super.onResume()
        // Reapply immersive mode when returning to the activity
        setupImmersiveMode()
    }
}
