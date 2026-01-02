# CameraOverlayUI Visual Design Specification

## Visual Hierarchy & Layout

```
┌─────────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │  ← Status Bar Scrim (100dp)
│  ▓▓▓▓▓▓▓▓▓▓ (gradient) ▓▓▓▓▓▓▓▓▓▓▓▓▓  │    Black 0.5α → Transparent
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │
│                                         │
│           Camera Preview                │
│         (Full Screen Layer)             │
│                                         │
│              ┏━━━━━━┓                   │  ← AR Canvas Layer
│              ┃      ┃                   │    (Reticle corners)
│       >>>    ┃  ·   ┃                   │    NeonLime when Perfect
│              ┃      ┃                   │    ElectricGold when Warning
│              ┗━━━━━━┛                   │    Pulsating arrows on sides
│                                         │
│                                         │
│     ┌───────────────────────────┐      │
│     │  ╔═══════════════════╗    │      │  ← Guidance HUD (150dp from bottom)
│     │  ║   PERFECT         ║    │      │    Frosted glass background
│     │  ║ Take The Shot     ║    │      │    Cinzel Bold (main)
│     │  ╚═══════════════════╝    │      │    Montserrat Medium (sub)
│     └───────────────────────────┘      │
│                                         │
│             ⊙━━━━━⊙                     │  ← Shutter Button (48dp from bottom)
│             ┃     ┃                     │    White ring + white circle
│             ⊙━━━━━⊙                     │    Scale animation on press
│                                         │
└─────────────────────────────────────────┘
```

## Component Details

### 1. GlassBackground Effect
```
Visual Appearance:
┌─────────────────────────────┐
│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░│  ← Subtle white border (0.10α)
│░                           ░│
│░    Frosted Glass Area    ░│  ← Blurred background
│░    (Black gradient)      ░│     0.4α → 0.8α
│░                           ░│
│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░│
└─────────────────────────────┘
      24dp corner radius
```

### 2. GuidanceHUD States

#### Perfect State
```
┌───────────────────────────────┐
│  ╔═══════════════════════════╗│
│  ║     PERFECT              ║│  ← Cinzel Bold, 28sp
│  ║   (NeonLime #AEEA00)    ║│     Letter spacing: -0.5sp
│  ║                          ║│
│  ║   TAKE THE SHOT          ║│  ← Montserrat Medium, 11sp
│  ║   (OffWhite 0.8α)        ║│     Letter spacing: 2sp
│  ╚═══════════════════════════╝│
└───────────────────────────────┘
```

#### Warning State
```
┌───────────────────────────────┐
│  ╔═══════════════════════════╗│
│  ║  MOVE LEFT               ║│  ← Cinzel Bold, 28sp
│  ║  (ElectricGold #FFD700)  ║│     Letter spacing: -0.5sp
│  ║                          ║│
│  ║  ADJUST COMPOSITION      ║│  ← Montserrat Medium, 11sp
│  ║  (OffWhite 0.8α)         ║│     Letter spacing: 2sp
│  ╚═══════════════════════════╝│
└───────────────────────────────┘
```

#### Critical State
```
┌───────────────────────────────┐
│  ╔═══════════════════════════╗│
│  ║  TILT PHONE RIGHT        ║│  ← Cinzel Bold, 28sp
│  ║  (ErrorRed #FF5252)      ║│     Letter spacing: -0.5sp
│  ║                          ║│
│  ║  IMMEDIATE ACTION REQ'D  ║│  ← Montserrat Medium, 11sp
│  ║  (OffWhite 0.8α)         ║│     Letter spacing: 2sp
│  ╚═══════════════════════════╝│
└───────────────────────────────┘
```

### 3. ARCanvas Reticle

#### Normal State
```
        ┏━━━━━┓
        ┃     ┃
        ┃  ·  ┃    ← Thin elegant corners
        ┃     ┃       120dp size
        ┗━━━━━┛       3dp stroke
```

#### Perfect State (Snapped)
```
       ┏━━━┓
       ┃ · ┃        ← Scaled to 0.85
       ┗━━━┛           102dp size
                       4dp stroke
      ╭──────╮        Glow ring (NeonLime)
     │  ┏━━━┓ │
     │  ┃ · ┃ │
     │  ┗━━━┛ │
      ╰──────╯
```

### 4. Directional Arrows

#### Left Direction
```
Screen Layout:
┌──────────────────────────────┐
│                              │
│  >>> >>> >>>                 │  ← Pulsating chevrons
│  >>> >>> >>>    [Reticle]    │     Fade: 0.3α → 1.0α
│  >>> >>> >>>                 │     Move: 0 → 40dp
│                              │     800ms cycle
└──────────────────────────────┘
```

#### Right Direction
```
Screen Layout:
┌──────────────────────────────┐
│                              │
│                 <<< <<< <<<  │  ← Pulsating chevrons
│    [Reticle]    <<< <<< <<<  │     Fade: 0.3α → 1.0α
│                 <<< <<< <<<  │     Move: 0 → 40dp
│                              │     800ms cycle
└──────────────────────────────┘
```

#### Up Direction
```
Screen Layout:
┌──────────────────────────────┐
│           ^^^                │
│           ^^^                │  ← Pulsating chevrons
│           ^^^                │     pointing upward
│                              │
│        [Reticle]             │
│                              │
└──────────────────────────────┘
```

## Color Palette

```
State        Primary Color         Hex        RGB
────────────────────────────────────────────────────
Perfect      NeonLime          #AEEA00    174,234,0
Warning      ElectricGold      #FFD700    255,215,0
Critical     ErrorRed          #FF5252    255,82,82

Background   RichBlack         #121212    18,18,18
Surface      DarkSurface       #1E1E1E    30,30,30
Text         OffWhite          #E0E0E0    224,224,224
```

## Animation Timelines

### Text Change Animation (GuidanceHUD)
```
Time:  0ms ─────────────────────────────────────→ 600ms
       │                                         │
State: OLD TEXT                           NEW TEXT
       │                                         │
       ├─ Slide Out (-50% Y) ──→ (300ms)
       └─ Fade Out ──────────→ (300ms)
                  └─ Slide In (+50% Y) ─→ Spring
                  └─ Fade In ───────────→ (300ms)
```

### Reticle Snap Animation (Perfect State)
```
Time:  0ms ──────────────────────────────────→ ~1000ms
       │                                       │
Scale: 1.0 ───────────────────────────────→ 0.85
       │                                       │
       └─ Spring (Medium Bouncy, Medium Stiff)
          Overshoots then settles
```

### Arrow Pulse Animation
```
Time:  0ms ────→ 800ms ────→ 1600ms ────→ (repeats)
       │         │            │
Alpha: 0.3 ───→ 1.0 ───→ 0.3 ───→ 1.0
Offset: 0 ────→ 40dp ──→ 0 ───→ 40dp
       │         │            │
       └─────────┴────────────┴─ Infinite Repeatable
```

## Typography Specifications

### Cinzel Bold (Main Instructions)
- Font: Cinzel Bold (serif)
- Size: 28sp
- Letter Spacing: -0.5sp (tight)
- Transform: UPPERCASE
- Color: Dynamic (state-based)
- Alignment: Center

### Montserrat Medium (Sub Instructions)
- Font: Montserrat Medium (sans-serif)
- Size: 11sp
- Letter Spacing: 2sp (wide)
- Transform: UPPERCASE
- Color: OffWhite 0.8α
- Alignment: Center

## Interaction Feedback

### Shutter Button Press
```
Normal State      Pressed         Released
     ⊙                 ⊙               ⊙
     │                 │               │
Scale: 1.0 ────→ 0.85 ────→ 1.0
Time:  0ms      100ms       200ms
Curve: Tween (linear)
```

## Accessibility Considerations

1. **Color Contrast**
   - NeonLime on Black: 9.8:1 (AAA)
   - ElectricGold on Black: 11.2:1 (AAA)
   - ErrorRed on Black: 5.1:1 (AA)
   - OffWhite on Black: 12.6:1 (AAA)

2. **Text Readability**
   - Minimum text size: 11sp (sub-instructions)
   - Main instructions: 28sp (highly legible)
   - Frosted glass provides contrast against any background

3. **Animation Duration**
   - All animations < 1000ms
   - No rapid flashing (800ms pulse is safe)
   - Smooth easing curves prevent disorientation

## Dark Mode Optimization

All colors are pre-optimized for dark backgrounds:
- No pure white (uses OffWhite #E0E0E0)
- Reduced eye strain with lower brightness
- Glass effects provide depth without glare
- Accent colors pop against dark background

## Performance Notes

- **Canvas Drawing**: Zero view hierarchy overhead
- **Animation**: GPU-accelerated where possible
- **Blur Effect**: Hardware accelerated on Android 12+
- **Target FPS**: 60fps for all animations
- **Memory**: Minimal allocation (reused composables)

---

**Design Language**: Dark Luxury • High Fashion • Premium Tech
**Target Audience**: Fashion photography enthusiasts
**Platform**: Android 7.0+ (SDK 24+)
**Optimal**: Android 12+ (SDK 31+) for full effects
