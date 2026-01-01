package com.angl.di

import com.angl.data.repository.CameraManager
import com.angl.domain.repository.CameraRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides camera-related dependencies.
 * 
 * This module binds the CameraManager implementation to the CameraRepository interface,
 * following the Dependency Inversion Principle of Clean Architecture.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {
    
    /**
     * Binds CameraManager implementation to CameraRepository interface.
     * This allows the rest of the app to depend on the abstraction rather than
     * the concrete implementation.
     */
    @Binds
    @Singleton
    abstract fun bindCameraRepository(
        cameraManager: CameraManager
    ): CameraRepository
}
