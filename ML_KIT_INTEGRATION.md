# ML Kit Integration Guide

## Overview

This document explains the ML Kit Pose Detection integration and the critical coordinate mapping system implemented in Angl.

## Components

### 1. PoseAnalyzer

**Location:** `app/src/main/java/com/angl/data/analyzer/PoseAnalyzer.kt`

The `PoseAnalyzer` implements `ImageAnalysis.Analyzer` to process camera frames in real-time.

#### Key Features

- **Accurate Model**: Uses ML Kit's Accurate Pose Detector for high-quality landmark detection
- **Rotation Handling**: Correctly handles device orientation via `imageProxy.imageInfo.rotationDegrees`
- **StateFlow Integration**: Emits detected poses for ViewModel observation
- **Frame Management**: Critical `imageProxy.close()` prevents frame drops
- **Performance**: Processing flag prevents queue buildup

#### Implementation Details

```kotlin
@Singleton
class PoseAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {
    
    private val _poseFlow = MutableStateFlow<Pose?>(null)
    val poseFlow: StateFlow<Pose?> = _poseFlow.asStateFlow()
    
    private val poseDetector: PoseDetector by lazy {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        PoseDetection.getClient(options)
    }
    
    override fun analyze(imageProxy: ImageProxy) {
        // Critical: Convert with rotation
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        poseDetector.process(inputImage)
            .addOnCompleteListener {
                // Critical: Always close to receive next frame
                imageProxy.close()
            }
    }
}
```

#### Rotation Degrees Explained

The `imageProxy.imageInfo.rotationDegrees` is critical because:
- Device can be in any orientation (0°, 90°, 180°, 270°)
- Camera sensor has fixed orientation
- ML Kit needs correct orientation for landmark detection
- Wrong rotation = incorrect or no pose detection

**Example:**
- Device upright: 0° rotation
- Device landscape right: 90° rotation
- Device upside down: 180° rotation
- Device landscape left: 270° rotation

#### Frame Management

**Why closing ImageProxy is critical:**

```kotlin
// ❌ BAD - Causes frame drops
override fun analyze(imageProxy: ImageProxy) {
    // Process image
    // Forget to close -> frames pile up -> FPS drops
}

// ✅ GOOD - Maintains 60fps
override fun analyze(imageProxy: ImageProxy) {
    try {
        // Process image
    } finally {
        imageProxy.close() // Always close, even on error
    }
}
```

Without closing:
1. New frames can't be delivered
2. Queue fills up
3. FPS drops to 1-2 fps
4. Unresponsive camera

### 2. CoordinateMapper

**Location:** `app/src/main/java/com/angl/util/CoordinateMapper.kt`

The `CoordinateMapper` solves "coordinate hell" - mapping between camera space and screen space.

#### The Problem

**Camera Space:**
- Typical resolution: 640x480, 1280x720, 1920x1080
- Aspect ratio: 4:3 or 16:9
- Origin: Top-left corner
- Coordinates: (0,0) to (width, height)

**Screen Space:**
- Various resolutions: 1080x2400, 1440x3200, etc.
- Aspect ratio: 18:9, 19.5:9, 20:9, etc.
- Origin: Top-left corner
- Coordinates: (0,0) to (screenWidth, screenHeight)

**The Challenge:**
When camera aspect ratio ≠ screen aspect ratio, the preview is scaled and potentially cropped. Without proper mapping, AR overlays appear misaligned.

#### Mapping Logic

```kotlin
fun mapPoint(
    x: Float,
    y: Float,
    sourceSize: IntSize,
    targetSize: IntSize,
    isMirrored: Boolean = false
): Offset
```

**Steps:**

1. **Calculate aspect ratios**
   ```kotlin
   sourceAspect = 640 / 480 = 1.33 (4:3)
   targetAspect = 1080 / 2400 = 0.45 (18:9)
   ```

2. **Determine scale factor**
   ```kotlin
   // Source wider -> scale by height
   scale = targetHeight / sourceHeight
   
   // Source taller -> scale by width
   scale = targetWidth / sourceWidth
   ```

3. **Calculate offsets for centering**
   ```kotlin
   scaledWidth = sourceWidth * scale
   offsetX = (targetWidth - scaledWidth) / 2
   ```

4. **Apply transformation**
   ```kotlin
   screenX = (cameraX * scale) + offsetX
   screenY = (cameraY * scale) + offsetY
   ```

5. **Handle mirroring (front camera)**
   ```kotlin
   if (isMirrored) {
       cameraX = sourceWidth - cameraX
   }
   ```

#### Example Scenario

**Camera:** 640x480 (4:3 aspect)
**Screen:** 1080x2400 (18:9 aspect)

1. Source aspect: 1.33
2. Target aspect: 0.45
3. Source is wider → scale by height
4. Scale factor: 2400 / 480 = 5.0
5. Scaled width: 640 * 5.0 = 3200px
6. Offset X: (1080 - 3200) / 2 = -1060px (negative = cropped)
7. Landmark at (320, 240) maps to:
   - X: (320 * 5.0) - 1060 = 540px (center)
   - Y: (240 * 5.0) = 1200px (center)

#### Utility Functions

**Batch Mapping:**
```kotlin
val mappedLandmarks = CoordinateMapper.mapPoints(
    points = landmarks.map { (x, y) -> x to y },
    sourceSize = IntSize(640, 480),
    targetSize = screenSize,
    isMirrored = false
)
```

**Visible Region:**
```kotlin
val (visibleWidth, visibleHeight) = CoordinateMapper.calculateVisibleRegion(
    sourceSize = IntSize(640, 480),
    targetSize = IntSize(1080, 2400)
)
// Returns which part of camera image is actually visible
```

**Inverse Mapping (Touch Events):**
```kotlin
val cameraCoord = CoordinateMapper.screenToCamera(
    screenX = touchX,
    screenY = touchY,
    sourceSize = cameraSize,
    targetSize = screenSize,
    isMirrored = false
)
// Convert screen touch to camera coordinates
```

### 3. PoseOverlay

**Location:** `app/src/main/java/com/angl/presentation/camera/PoseOverlay.kt`

Composable Canvas that renders pose landmarks and skeleton over camera preview.

#### Features

- Maps all 33 pose landmarks to screen coordinates
- Draws landmarks as circles
- Connects landmarks with skeleton lines
- Handles coordinate transformation automatically

#### Usage

```kotlin
@Composable
fun CameraScreen() {
    Box {
        // Camera preview
        CameraPreviewContent()
        
        // Pose overlay on top
        PoseOverlay(
            pose = detectedPose,
            sourceSize = IntSize(640, 480),
            isMirrored = false
        )
    }
}
```

#### Landmark Connections

Standard pose skeleton includes:
- **Face**: Ears → Eyes → Nose
- **Upper Body**: Shoulders → Elbows → Wrists
- **Torso**: Shoulders ↔ Hips
- **Lower Body**: Hips → Knees → Ankles

Total: 33 landmarks, ~17 major connections

## Integration Flow

### 1. Camera Setup
```kotlin
CameraManager(context, poseAnalyzer)
    .startCamera(previewView, lifecycleOwner)
```

### 2. Frame Analysis
```
Camera Frame → ImageProxy → PoseAnalyzer
    ↓
Convert to InputImage with rotation
    ↓
ML Kit Pose Detection
    ↓
Emit Pose via StateFlow
    ↓
Close ImageProxy
```

### 3. Coordinate Mapping
```
Pose Landmarks (camera space)
    ↓
CoordinateMapper.mapPoint()
    ↓
Screen Coordinates
    ↓
PoseOverlay renders
```

### 4. UI Update
```
ViewModel observes poseFlow
    ↓
StateFlow emits to Composable
    ↓
PoseOverlay recomposes
    ↓
Canvas draws landmarks
```

## Performance Considerations

### Target: 60fps

**Optimizations:**
1. **ML Kit Model**: Accurate model with SINGLE_IMAGE_MODE
2. **Resolution**: 640x480 for fast processing
3. **Frame Strategy**: STRATEGY_KEEP_ONLY_LATEST drops old frames
4. **Processing Flag**: Prevents simultaneous processing
5. **Immediate Close**: ImageProxy closed in complete listener
6. **Efficient Mapping**: O(1) coordinate transformation

### Benchmark Targets

- **Frame Analysis**: <16ms (60fps)
- **ML Kit Processing**: 10-30ms (Accurate model)
- **Coordinate Mapping**: <1ms (simple math)
- **Canvas Drawing**: 2-5ms (GPU accelerated)

### Memory Management

- **PoseAnalyzer**: Singleton, single detector instance
- **Lazy Initialization**: Detector created on first use
- **Proper Cleanup**: `poseAnalyzer.close()` releases ML Kit resources
- **StateFlow**: No memory leaks, automatic cleanup

## Testing

### Unit Tests

```kotlin
@Test
fun testCoordinateMapping() {
    val result = CoordinateMapper.mapPoint(
        x = 320f,
        y = 240f,
        sourceSize = IntSize(640, 480),
        targetSize = IntSize(1080, 2400),
        isMirrored = false
    )
    
    assertEquals(540f, result.x, 0.1f) // Center X
    assertEquals(1200f, result.y, 0.1f) // Center Y
}

@Test
fun testMirroring() {
    val result = CoordinateMapper.mapPoint(
        x = 100f,
        y = 100f,
        sourceSize = IntSize(640, 480),
        targetSize = IntSize(640, 480),
        isMirrored = true
    )
    
    assertEquals(540f, result.x) // Mirrored X
    assertEquals(100f, result.y) // Y unchanged
}
```

### Integration Tests

1. **Rotation Handling**: Test all 4 orientations
2. **Aspect Ratios**: Test various screen sizes
3. **Frame Rate**: Measure actual FPS
4. **Accuracy**: Compare mapped vs actual positions

## Troubleshooting

### Pose Not Detected

**Issue**: ML Kit returns null or empty pose

**Solutions:**
1. Check rotation degrees are correct
2. Ensure adequate lighting
3. Full body should be visible
4. Minimum distance from camera

### Misaligned Overlay

**Issue**: Landmarks don't match body position

**Solutions:**
1. Verify source size matches camera resolution
2. Check target size matches screen size
3. Confirm isMirrored flag is correct
4. Test with different aspect ratios

### Low FPS

**Issue**: Camera preview stutters

**Solutions:**
1. Verify imageProxy.close() is always called
2. Check processing flag prevents concurrent analysis
3. Reduce resolution if needed
4. Profile ML Kit processing time

### Memory Leaks

**Issue**: App memory grows over time

**Solutions:**
1. Call poseAnalyzer.close() on cleanup
2. Ensure CameraManager.cleanup() is called
3. Check StateFlow observers are cleared
4. Verify no ImageProxy retention

## Future Enhancements

### 1. Multi-Person Pose Detection
Switch to STREAM_MODE for real-time tracking of multiple people.

### 2. Pose Classification
Add pose classification (sitting, standing, yoga poses) on top of detection.

### 3. Gesture Recognition
Analyze landmark sequences for gesture detection.

### 4. AR Guidance
Add visual guides (arrows, outlines) for photo composition.

### 5. Performance Modes
Allow switching between Accurate (quality) and Base (speed) models.

## References

- [ML Kit Pose Detection](https://developers.google.com/ml-kit/vision/pose-detection)
- [CameraX ImageAnalysis](https://developer.android.com/training/camerax/analyze)
- [Compose Canvas](https://developer.android.com/jetpack/compose/graphics/draw/overview)
