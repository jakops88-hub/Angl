# Quick Release Guide

This is a quick reference for creating releases of the Angl app.

## For CI/CD Releases (Recommended)

### Automatic Version Management
Every push to `main` branch or manual workflow trigger will:
1. Automatically use version code: `1000 + GitHub run number`
2. Build a signed AAB (if secrets are configured)
3. Create an artifact named `app-release-run-<number>`

### Steps
1. **Push to main** or **manually trigger** workflow from GitHub Actions tab
2. **Wait for build** to complete
3. **Download AAB** from workflow artifacts
4. **Upload to Google Play Console**

That's it! The version code is automatically unique for each build.

## For Local Releases (If Needed)

### Before Building
1. **Check current version**:
   ```bash
   ./gradlew showVersion
   ```

2. **Increment version code** (if building for Play Store):
   ```bash
   ./gradlew incrementVersionCode
   ```

3. **Update version name** (if needed):
   Edit `version.properties` and change:
   ```properties
   VERSION_MAJOR=1
   VERSION_MINOR=4  # Increment this
   VERSION_PATCH=0
   ```

### Build Release
```bash
# Build signed AAB
./gradlew bundleRelease

# Output will be at:
# app/build/outputs/bundle/release/app-release.aab
```

## Version Naming Convention

Follow semantic versioning:
- **MAJOR**: Incompatible API changes or major redesign
- **MINOR**: Add functionality in a backward-compatible manner
- **PATCH**: Backward-compatible bug fixes

Examples:
- `1.3.0` → `1.4.0` (new feature)
- `1.3.0` → `1.3.1` (bug fix)
- `1.3.0` → `2.0.0` (major redesign)

## Troubleshooting

### "Version code X has already been used"
**For CI builds**: Just trigger a new build - it will automatically use the next version code.

**For local builds**: Run `./gradlew incrementVersionCode` to bump the version code.

### Check what version will be built
```bash
./gradlew showVersion
```

### View build logs
The build process prints version information:
```
===========================================
Building Angl
Version Code: 1042
Version Name: 1.3.0
===========================================
```

## Quick Commands

```bash
# Show current version
./gradlew showVersion

# Increment version code for local release
./gradlew incrementVersionCode

# Build release AAB
./gradlew bundleRelease

# Clean build
./gradlew clean bundleRelease
```
