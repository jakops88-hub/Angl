package com.angl.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Application-level Hilt module for core dependencies.
 * 
 * Provides singleton instances for:
 * - Coroutine scopes for different purposes
 * - Application-wide utilities
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides a CoroutineScope specifically for image analysis operations.
     * 
     * This scope uses:
     * - Dispatchers.IO for background I/O operations
     * - SupervisorJob to prevent cancellation of other coroutines if one fails
     * 
     * Used by PoseAnalyzer and other ML processing components to avoid blocking UI.
     */
    @Provides
    @Singleton
    @ImageAnalysisScope
    fun provideImageAnalysisScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    /**
     * Provides a CoroutineScope for general application operations.
     * Uses Dispatchers.Default for CPU-intensive work.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

/**
 * Qualifier for image analysis coroutine scope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageAnalysisScope

/**
 * Qualifier for application-level coroutine scope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
