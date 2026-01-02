# CameraOverlayUI - Usage Guide

## Overview

The `CameraOverlayUI.kt` file provides a complete high-fashion UI overlay system for the Angl camera app. It implements a "Dark Luxury" design philosophy with premium animations and visual effects.

## Components

### 1. GlassModifier Extension

A reusable modifier that creates a frosted glass effect:

```kotlin
// Usage example
Box(
    modifier = Modifier
        .glassBackground()
        .padding(16.dp)
) {
    Text("Your content here")
}
```

**Features:**
- Vertical gradient (Black alpha 0.4 → 0.8)
- 1dp white border (alpha 0.10)
- 24dp corner radius
- Blur effect on Android 12+ using RenderEffect
- Automatic fallback to higher opacity on older Android versions

### 2. GuidanceHUD Component

Premium feedback box that displays composition guidance:

```kotlin
@Composable
fun YourScreen() {
    val feedbackState by viewModel.feedbackState.collectAsState()
    
    GuidanceHUD(
        feedbackState = feedbackState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 150.dp)
    )
}
```

**Features:**
- Main instruction in Cinzel Bold (uppercase, large)
- Sub-instruction in Montserrat Medium (small, 2sp letter spacing)
- Animated color transitions:
  - ErrorRed for Critical state
  - ElectricGold for Warning state
  - NeonLime for Perfect state
- Slide up & fade in animation on text changes

### 3. ARCanvas Component

Full-screen AR overlay with visual guidance:

```kotlin
@Composable
fun YourScreen() {
    val feedbackState by viewModel.feedbackState.collectAsState()
    
    ARCanvas(
        feedbackState = feedbackState,
        modifier = Modifier.fillMaxSize()
    )
}
```

**Features:**
- Center reticle with elegant corners
- Perfect state: Corners snap inward with spring animation + NeonLime glow
- Directional arrows: Pulsating chevrons for movement guidance
- Smooth color transitions based on feedback state

**Visual Behaviors:**
- **Perfect State**: Reticle scales to 0.85 with spring bounce, glows in NeonLime
- **Warning/Critical States**: Shows directional arrows based on GuidanceType
  - LEFT/RIGHT: Chevrons on side of screen
  - UP/DOWN: Chevrons at top/bottom of screen
  - Arrows pulse and fade in/out continuously

### 4. CameraScreenContent Layout

Complete camera screen with all layers assembled:

```kotlin
@Composable
fun CameraScreen(viewModel: CameraViewModel = hiltViewModel()) {
    val feedbackState by viewModel.feedbackState.collectAsState()
    
    CameraScreenContent(
        feedbackState = feedbackState,
        onTakePhoto = { viewModel.takePhoto() },
        cameraPreviewContent = {
            // Your camera preview composable
            CameraPreviewView()
        }
    )
}
```

**Layer Hierarchy (bottom to top):**
1. Camera Preview (fills screen)
2. ARCanvas overlay (fills screen)
3. Top gradient scrim (protects status bar, 100dp height)
4. GuidanceHUD (floats 150dp from bottom)
5. ShutterButton (bottom center, 48dp from bottom)

## Integration with Existing CameraScreen

You have two options to integrate the new UI:

### Option A: Use CameraScreenContent (Recommended for new implementations)

Replace the entire camera screen layout with `CameraScreenContent`:

```kotlin
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val feedbackState by viewModel.feedbackState.collectAsState()
    
    CameraScreenContent(
        feedbackState = feedbackState,
        onTakePhoto = { viewModel.takePhoto() },
        cameraPreviewContent = {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        viewModel.startCamera(this, lifecycleOwner)
                    }
                }
            )
        }
    )
}
```

### Option B: Use Individual Components (Keep existing structure)

Keep your current `CameraScreen.kt` and replace individual components:

```kotlin
// In your existing CameraScreen Success state:
when (cameraState) {
    is CameraState.Success -> {
        // Camera preview (existing)
        CameraPreviewContent(...)
        
        // Replace AnglOverlay with ARCanvas
        ARCanvas(
            feedbackState = feedbackState,
            modifier = Modifier.fillMaxSize()
        )
        
        // Add GuidanceHUD
        GuidanceHUD(
            feedbackState = feedbackState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp)
        )
        
        // ShutterButton (existing)
        ShutterButton(
            onClick = { viewModel.takePhoto() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}
```

## Design Specifications

### Colors (from Color.kt)
- **NeonLime** (`#AEEA00`): Perfect state
- **ElectricGold** (`#FFD700`): Warning state
- **ErrorRed** (`#FF5252`): Critical state
- **RichBlack** (`#121212`): Background
- **OffWhite** (`#E0E0E0`): Secondary text

### Typography (from Type.kt)
- **Cinzel Bold**: Main instructions (uppercase, tight letter spacing -0.5sp)
- **Montserrat Medium**: Sub-instructions (wide letter spacing 2sp)
- **Montserrat Bold**: Buttons and labels

### Animation Timings
- Color transitions: 500-600ms with FastOutSlowInEasing
- Spring animations: Medium bouncy damping, medium stiffness
- Arrow pulse: 800ms infinite repeatable
- Text slide-in: Spring animation with fade

## Android Version Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Blur Effects**: Requires Android 12+ (SDK 31)
  - Automatic fallback to higher opacity on older versions

## Performance Considerations

1. **Efficient Animations**: Uses `rememberInfiniteTransition` for smooth looping animations
2. **Minimal Recomposition**: State changes are localized to specific components
3. **Hardware Acceleration**: Blur effects use GPU when available
4. **Canvas Drawing**: All AR visuals use efficient Canvas drawing (no view hierarchy)

## Testing

To test the components:

1. **Perfect State**: Ensure proper shoulder alignment and good framing
   - Reticle should snap inward with spring animation
   - Color should be NeonLime
   - Text should read "PERFECT" / "TAKE THE SHOT"

2. **Warning State**: Introduce minor misalignment
   - Color should be ElectricGold
   - Directional arrows should appear if needed
   - Text should show specific guidance

3. **Critical State**: Create major composition issues
   - Color should be ErrorRed
   - Arrows should pulse faster
   - Text should show urgent guidance

## Troubleshooting

### Blur effect not working
- Check Android version (must be 12+)
- Fallback is automatic, no action needed

### Chevrons not appearing
- Verify `FeedbackStateExtended` includes correct `GuidanceType`
- Check that `TranslationDirection` is set properly

### Text not animating
- Ensure `feedbackState` value actually changes
- Check that `AnimatedContent` targetState is correctly bound

## Credits

Design Philosophy: "Dark Luxury" - High-Fashion Tech
Fonts: Cinzel (serif), Montserrat (sans-serif)
Animations: Spring physics and easing curves
