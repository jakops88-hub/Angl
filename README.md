# Angl
AI Photo Coach - Real-time Camera Assistant with ML Kit

## Overview
Angl is a modern Android application that uses Google ML Kit to guide users in taking better photos. The app provides real-time camera feedback with a focus on performance (60fps processing) and user experience.

## Architecture

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material3
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection:** Hilt
- **Camera:** CameraX
- **ML Processing:** Google ML Kit (Pose Detection)

### Project Structure
```
app/
├── src/main/java/com/angl/
│   ├── AnglApplication.kt           # Hilt application entry point
│   ├── data/
│   │   └── repository/
│   │       └── CameraManager.kt     # CameraX implementation
│   ├── di/
│   │   └── CameraModule.kt          # Hilt dependency injection module
│   ├── domain/
│   │   ├── model/
│   │   │   ├── CameraError.kt       # Error types
│   │   │   └── CameraResult.kt      # Result wrapper
│   │   └── repository/
│   │       └── CameraRepository.kt  # Repository interface
│   └── presentation/
│       ├── MainActivity.kt          # Main activity
│       ├── camera/
│       │   └── CameraScreen.kt      # Camera UI composable
│       ├── viewmodel/
│       │   └── CameraViewModel.kt   # Camera state management
│       └── theme/
│           ├── Color.kt             # Material3 colors
│           ├── Theme.kt             # App theme
│           └── Type.kt              # Typography
```

## Key Features Implemented

### 1. CameraManager Class
Located in `data/repository/CameraManager.kt`, this class wraps the CameraX API with:

- **Performance Mode Configuration:** Uses `STRATEGY_KEEP_ONLY_LATEST` for low-latency processing
- **Optimized Resolution:** 640x480 for ML processing (balances quality and performance)
- **Lifecycle Management:** Properly binds to Compose lifecycle
- **Error Handling:** Comprehensive error checking for permissions and hardware availability
- **Memory Safety:** Prevents leaks through proper resource management

### 2. Clean Architecture Layers

#### Domain Layer
- **CameraRepository:** Interface defining camera operations
- **CameraResult:** Sealed class for type-safe result handling
- **CameraError:** Sealed class for specific error types

#### Data Layer
- **CameraManager:** Concrete implementation of CameraRepository
- Handles all CameraX API interactions

#### Presentation Layer
- **CameraViewModel:** Manages camera state using StateFlow
- **CameraScreen:** Composable UI with permission handling
- **Material3 Theme:** Modern UI design

### 3. Camera Configuration

The camera is configured for optimal real-time performance:

```kotlin
// Back camera selection
val cameraSelector = CameraSelector.Builder()
    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
    .build()

// Low-latency image analysis
val imageAnalysis = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .setTargetResolution(android.util.Size(640, 480))
    .build()
```

**Key Performance Features:**
- `STRATEGY_KEEP_ONLY_LATEST`: Drops frames if processing is slow (critical for 60fps)
- 640x480 resolution: Faster ML processing while maintaining quality
- Hardware acceleration: Enabled in PreviewView

### 4. Permission Handling

The app requests camera permissions at runtime and handles all states:
- Permission granted → Start camera
- Permission denied → Show explanation and request button
- Camera not available → Show error message

### 5. Error Handling

Comprehensive error handling covers:
- `PermissionDenied`: Camera permission not granted
- `CameraNotAvailable`: No camera hardware
- `InitializationFailed`: Camera binding failed
- `Unknown`: Unexpected errors

## Building the Project

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (minimum)
- Android SDK 34 (target)
- JDK 17

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Code Quality Standards

### Null Safety
All code is null-safe with proper use of:
- Nullable types (`?`)
- Safe calls (`?.`)
- Elvis operator (`?:`)
- Non-null assertions only when guaranteed safe

### Memory Leak Prevention
- Proper lifecycle binding with CameraX
- ViewModel cleanup in `onCleared()`
- ExecutorService shutdown in cleanup
- StateFlow instead of LiveData (better Compose integration)

### Performance Optimization
- Coroutines for async operations
- Background executors for camera operations
- Minimal recomposition in Compose
- Efficient state management with StateFlow

## Next Steps (Future Development)

The foundation is now ready for:
1. ML Kit Pose Detection integration
2. Real-time guidance overlays
3. Photo composition analysis
4. Golden ratio grid overlay
5. Rule of thirds guidance
6. Level indicator
7. Photo capture functionality

## Dependencies

Key dependencies used:
- **CameraX:** 1.3.1
- **Compose:** 2023.10.01 BOM
- **Hilt:** 2.48
- **ML Kit Pose Detection:** 18.0.0-beta4
- **Kotlin:** 1.9.20
- **Coroutines:** 1.7.3

## License

[Add your license here]

