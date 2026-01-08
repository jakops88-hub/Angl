package com.angl.presentation

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import java.io.PrintWriter
import java.io.StringWriter

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
        // Install global exception handler FIRST (before any other code)
        setupGlobalExceptionHandler()
        
        try {
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
        } catch (e: Exception) {
            // Catch any exceptions during initialization and display them,
            // then finish the activity stack to avoid continuing in an inconsistent state.
            showFatalErrorDialog(e)
            finishAffinity()
        }
    }
    
    /**
     * Sets up a global uncaught exception handler to display crash details on screen.
     * This allows developers to see stack traces on physical devices without logcat access.
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Show error dialog on the UI thread if activity is valid
                if (!isFinishing && !isDestroyed) {
                    runOnUiThread {
                        showFatalErrorDialog(throwable)
                    }
                }
            } catch (e: Exception) {
                // If we can't show the dialog, at least try the default handler
                e.printStackTrace()
            }
            
            // Call the original handler to ensure proper cleanup
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * Displays a detailed error dialog with the full stack trace.
     * This allows users to screenshot the error and send it for debugging.
     */
    private fun showFatalErrorDialog(throwable: Throwable) {
        try {
            val stackTrace = getStackTraceString(throwable)
            val errorMessage = "FATAL ERROR:\n\n${throwable.javaClass.simpleName}: ${throwable.message}\n\n$stackTrace"
            
            AlertDialog.Builder(this)
                .setTitle("App Crashed - Debug Info")
                .setMessage(errorMessage)
                .setCancelable(true)
                .setOnCancelListener {
                    // Ensure the app can always be exited even if buttons misbehave
                    finishAffinity()
                }
                .setPositiveButton("Close App") { _, _ ->
                    finishAffinity()
                }
                .setNeutralButton("Copy Error") { _, _ ->
                    copyToClipboard(errorMessage)
                    finishAffinity()
                }
                .show()
        } catch (e: Exception) {
            // Last resort: if we can't even show the dialog, at least print
            e.printStackTrace()
            throwable.printStackTrace()
        }
    }
    
    /**
     * Converts a Throwable's stack trace to a readable String.
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        return stringWriter.toString()
    }
    
    /**
     * Copies text to the system clipboard.
     */
    private fun copyToClipboard(text: String) {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Error Log", text)
            clipboard.setPrimaryClip(clip)
        } catch (e: Exception) {
            e.printStackTrace()
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
