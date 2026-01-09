package com.angl

import android.os.Bundle
import android.util.TypedValue
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that displays crash information when an uncaught exception occurs.
 * This allows developers to see stack traces on physical devices without logcat access.
 */
class CrashActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_ERROR_TEXT = "error_text"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the error text from the intent
        val errorText = intent.getStringExtra(EXTRA_ERROR_TEXT) ?: "Unknown error occurred"
        
        // Convert DP to pixels for padding
        val paddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            16f, 
            resources.displayMetrics
        ).toInt()
        
        // Create TextView programmatically
        val textView = TextView(this).apply {
            text = errorText
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f) // Size in SP units for proper scaling
            setTextIsSelectable(true) // Allow user to select and copy text
        }
        
        // Create ScrollView and add TextView
        val scrollView = ScrollView(this).apply {
            addView(textView)
        }
        
        setContentView(scrollView)
    }
}
