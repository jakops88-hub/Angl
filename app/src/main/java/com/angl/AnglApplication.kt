package com.angl

import android.app.Application
import android.content.Intent
import dagger.hilt.android.HiltAndroidApp
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Main Application class for Angl.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class AnglApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        setupGlobalExceptionHandler()
    }
    
    /**
     * Sets up a global uncaught exception handler to catch crashes
     * and display the stack trace on screen via CrashActivity.
     * This allows developers to see errors on physical devices without logcat access.
     */
    private fun setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Get the full stack trace as a string
                val stackTrace = getStackTraceString(throwable)
                val errorMessage = "FATAL ERROR\n\n" +
                        "Exception: ${throwable.javaClass.name}\n" +
                        "Message: ${throwable.message}\n\n" +
                        "Stack Trace:\n$stackTrace"
                
                // Start CrashActivity to display the error
                val intent = Intent(this, CrashActivity::class.java).apply {
                    putExtra(CrashActivity.EXTRA_ERROR_TEXT, errorMessage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                
                // Exit the app to allow CrashActivity to display
                android.os.Process.killProcess(android.os.Process.myPid())
            } catch (e: Exception) {
                // If our handler fails, at least try to log it
                e.printStackTrace()
                throwable.printStackTrace()
                // Exit anyway
                android.os.Process.killProcess(android.os.Process.myPid())
            }
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
}
