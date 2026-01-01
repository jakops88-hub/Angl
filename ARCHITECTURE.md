# Angl Architecture Documentation

## Overview
This document explains the architecture decisions and implementation details of the Angl camera assistant application.

## Architecture Pattern: Clean Architecture + MVVM

### Why Clean Architecture?
Clean Architecture provides clear separation of concerns:
1. **Domain Layer:** Business logic and entities (independent of frameworks)
2. **Data Layer:** Data sources and repository implementations
3. **Presentation Layer:** UI and ViewModels

### Why MVVM?
MVVM (Model-View-ViewModel) works perfectly with Jetpack Compose:
- **View:** Composable functions (CameraScreen)
- **ViewModel:** Manages UI state and business logic (CameraViewModel)
- **Model:** Data and business entities (CameraResult, CameraError)

## Dependency Flow

```
Presentation Layer
    ↓ (depends on)
Domain Layer (abstractions)
    ↑ (implements)
Data Layer
```

This follows the **Dependency Inversion Principle**: high-level modules depend on abstractions, not implementations.

## Component Breakdown

### 1. Domain Layer (Pure Kotlin)

**Purpose:** Define business rules and contracts

**Components:**
- `CameraRepository` interface: Contract for camera operations
- `CameraResult<T>`: Sealed class for type-safe results
- `CameraError`: Sealed class for specific errors

**Benefits:**
- Framework independent
- Easily testable
- Clear contracts

### 2. Data Layer

**Purpose:** Implement domain contracts with actual data sources

**Components:**
- `CameraManager`: Implements `CameraRepository` using CameraX

**Key Responsibilities:**
```kotlin
class CameraManager(context: Context) : CameraRepository {
    // 1. Permission checking
    // 2. Hardware availability verification
    // 3. CameraX configuration and lifecycle management
    // 4. Error handling and reporting
    // 5. Resource cleanup
}
```

**Performance Optimizations:**
- Single thread executor for camera operations
- Suspending functions for non-blocking operations
- Proper lifecycle binding to prevent leaks

### 3. Presentation Layer

**Purpose:** Display UI and handle user interactions

**Components:**

#### CameraViewModel
```kotlin
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository
) : ViewModel() {
    private val _cameraState = MutableStateFlow<CameraState>(Initial)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    
    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
    fun stopCamera()
}
```

**State Management:**
```kotlin
sealed class CameraState {
    object Initial : CameraState()
    object Loading : CameraState()
    object Success : CameraState()
    data class Error(val error: CameraError, val message: String) : CameraState()
}
```

Benefits:
- Type-safe state representation
- Exhaustive when expressions
- Easy state transitions
- No invalid states possible

#### CameraScreen (Composable)
Handles:
- Permission requests
- State-based UI rendering
- Camera preview display
- Error display and retry

## Dependency Injection with Hilt

### Setup
```kotlin
@HiltAndroidApp
class AnglApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity()

@HiltViewModel
class CameraViewModel @Inject constructor(...)
```

### Module Configuration
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {
    @Binds
    @Singleton
    abstract fun bindCameraRepository(
        cameraManager: CameraManager
    ): CameraRepository
}
```

**Benefits:**
- Automatic dependency injection
- Easy testing (can inject mocks)
- Singleton scope for camera manager
- Compile-time verification

## Camera Configuration Details

### CameraX Use Cases

#### Preview
```kotlin
val preview = Preview.Builder()
    .build()
    .also { it.setSurfaceProvider(previewView.surfaceProvider) }
```
- Real-time display
- Automatic aspect ratio handling
- GPU-accelerated rendering

#### ImageAnalysis
```kotlin
val imageAnalysis = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .setTargetResolution(android.util.Size(640, 480))
    .build()
```

**STRATEGY_KEEP_ONLY_LATEST explained:**
- Drops frames if analyzer is busy
- Prevents queue buildup
- Ensures low latency
- Critical for 60fps goal

**Resolution 640x480:**
- Balance between quality and performance
- Faster ML inference
- Adequate for pose detection
- Can be adjusted based on device capabilities

### Lifecycle Binding
```kotlin
camera = provider.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview,
    imageAnalysis
)
```

**Benefits:**
- Automatic start/stop with lifecycle
- No manual cleanup needed
- Prevents resource leaks
- Handles configuration changes

## Error Handling Strategy

### Sealed Class Hierarchy
```kotlin
sealed class CameraError : Throwable() {
    object PermissionDenied : CameraError()
    object CameraNotAvailable : CameraError()
    data class InitializationFailed(override val message: String) : CameraError()
    data class Unknown(override val message: String) : CameraError()
}
```

### Result Wrapper
```kotlin
sealed class CameraResult<out T> {
    data class Success<T>(val data: T) : CameraResult<T>()
    data class Error(val exception: Throwable, val message: String) : CameraResult<Nothing>()
}
```

**Benefits:**
- Type-safe error handling
- No null checks needed
- Exhaustive when expressions
- Clear success/failure paths

## Memory Management

### Preventing Leaks

1. **ViewModel Cleanup:**
```kotlin
override fun onCleared() {
    super.onCleared()
    cameraRepository.stopCamera()
}
```

2. **CameraX Lifecycle Binding:**
- Automatically releases resources when lifecycle ends

3. **ExecutorService Shutdown:**
```kotlin
fun cleanup() {
    stopCamera()
    cameraExecutor.shutdown()
}
```

4. **StateFlow vs LiveData:**
- StateFlow is cold and doesn't retain observers
- Better for Compose
- Automatic cleanup

### Coroutine Management
- All coroutines launched in `viewModelScope`
- Automatically cancelled when ViewModel is cleared
- No leaked coroutines

## Performance Considerations

### 60fps Target
To achieve 60fps real-time processing:

1. **Low Resolution:** 640x480 reduces pixel count by ~75% vs HD
2. **Frame Dropping:** STRATEGY_KEEP_ONLY_LATEST prevents backlog
3. **Hardware Acceleration:** GPU rendering enabled
4. **Background Processing:** Camera operations on dedicated thread
5. **Efficient State Updates:** StateFlow only emits on changes

### Future ML Kit Integration
When adding pose detection:
```kotlin
imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
    // Process image with ML Kit
    // Extract pose landmarks
    // Draw guidance overlays
    imageProxy.close() // Must close to receive next frame
}
```

## Testing Strategy

### Unit Tests
- **Domain Layer:** Test business logic, result handling
- **ViewModel:** Test state transitions, error handling
- **Repository:** Mock CameraProvider, test configurations

### Integration Tests
- **Camera Initialization:** Test full flow with real CameraX
- **Permission Handling:** Test permission states
- **Error Scenarios:** Test hardware failures, permissions

### UI Tests
- **Compose Testing:** Test UI states, permission dialogs
- **Screenshot Tests:** Verify layouts

## Security Considerations

1. **Permission Model:**
   - Runtime permission requests
   - Graceful handling of denials
   - No assumptions about permissions

2. **Hardware Access:**
   - Check availability before access
   - Handle missing features gracefully
   - No crashes on unsupported devices

3. **Resource Cleanup:**
   - Always release camera resources
   - Prevent unauthorized background access
   - Proper lifecycle management

## Scalability

### Adding New Features

To add new ML Kit features:
1. Create new use case in domain layer
2. Implement in data layer
3. Update ViewModel state
4. Add UI in Composable

Example: Face Detection
```kotlin
// Domain
interface FaceDetectionRepository {
    suspend fun detectFaces(imageProxy: ImageProxy): Result<List<Face>>
}

// Data
class FaceDetectionManager @Inject constructor() : FaceDetectionRepository {
    // Implementation
}

// Presentation
sealed class FaceDetectionState { /* states */ }
```

### Multi-Camera Support
Architecture supports multiple cameras:
- Add camera selector parameter
- Update UI to select front/back
- CameraManager already supports it

## Conclusion

This architecture provides:
- ✅ Clean separation of concerns
- ✅ Easy testing
- ✅ Type safety
- ✅ Memory efficiency
- ✅ Performance optimization
- ✅ Scalability
- ✅ Maintainability

The foundation is solid for adding ML Kit features and building the AI photo coach functionality.
