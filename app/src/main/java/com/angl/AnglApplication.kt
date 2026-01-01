package com.angl

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for Angl.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class AnglApplication : Application()
