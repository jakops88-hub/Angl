package com.angl.util.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VibrationHelper provides haptic feedback for user interactions.
 * 
 * This class:
 * - Handles different vibration patterns for different events
 * - Manages API level compatibility (VibrationEffect vs deprecated methods)
 * - Provides debouncing to prevent rapid-fire vibrations
 * - Offers predefined patterns for success, warning, and error states
 */
@Singleton
class VibrationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService<VibratorManager>()
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService<Vibrator>()
    }

    private var lastVibrationTime = 0L
    private val debounceInterval = 300L // Minimum time between vibrations

    /**
     * Triggers a success vibration pattern.
     * Used when composition is perfect (lock-on achieved).
     * 
     * Pattern: Single strong pulse (100ms)
     */
    fun vibrateSuccess() {
        if (!shouldVibrate()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            vibrator?.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
        
        lastVibrationTime = System.currentTimeMillis()
    }

    /**
     * Triggers a warning vibration pattern.
     * Used for minor composition issues.
     * 
     * Pattern: Two quick pulses
     */
    fun vibrateWarning() {
        if (!shouldVibrate()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 50, 50)
            val amplitudes = intArrayOf(0, 100, 0, 100)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 50, 50, 50)
            vibrator?.vibrate(pattern, -1)
        }
        
        lastVibrationTime = System.currentTimeMillis()
    }

    /**
     * Triggers a critical/error vibration pattern.
     * Used for major composition issues.
     * 
     * Pattern: Three strong pulses
     */
    fun vibrateCritical() {
        if (!shouldVibrate()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 100, 50, 100, 50, 100)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 100, 50, 100, 50, 100)
            vibrator?.vibrate(pattern, -1)
        }
        
        lastVibrationTime = System.currentTimeMillis()
    }

    /**
     * Triggers a confirm vibration (for Perfect state transition).
     * This is the "lock-on" haptic that feels satisfying.
     */
    fun vibrateConfirm() {
        if (!shouldVibrate()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            vibrator?.vibrate(effect)
        } else {
            vibrateSuccess()
        }
        
        lastVibrationTime = System.currentTimeMillis()
    }

    /**
     * Checks if enough time has passed since last vibration (debouncing).
     */
    private fun shouldVibrate(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastVibrationTime) >= debounceInterval
    }

    /**
     * Cancels any ongoing vibration.
     */
    fun cancel() {
        vibrator?.cancel()
    }
}

/**
 * SoundHelper provides audio feedback for user interactions.
 * 
 * This class:
 * - Manages sound effects using SoundPool for low-latency playback
 * - Provides debouncing to prevent rapid-fire sounds
 * - Offers predefined sounds for success and feedback events
 */
@Singleton
class SoundHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool
    private var successSoundId: Int = 0
    private var lastSoundTime = 0L
    private val debounceInterval = 300L

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        // Note: Sound effects intentionally not loaded in this version
        // In production, add custom sound files to res/raw/ and load them here:
        // successSoundId = soundPool.load(context, R.raw.success_sound, 1)
        // 
        // For now, the app provides haptic-only feedback which is already
        // effective for the user experience. Sound can be added in future versions.
    }

    /**
     * Plays the success "lock-on" sound effect.
     * Called when perfect composition is achieved.
     */
    fun playSuccessSound() {
        if (!shouldPlaySound()) return
        
        if (successSoundId != 0) {
            soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
        
        lastSoundTime = System.currentTimeMillis()
    }

    /**
     * Checks if enough time has passed since last sound (debouncing).
     */
    private fun shouldPlaySound(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastSoundTime) >= debounceInterval
    }

    /**
     * Releases sound pool resources.
     * Note: As a singleton, this should only be called when the app is shutting down.
     * Currently unused but provided for completeness.
     */
    fun release() {
        soundPool.release()
    }
}