# Version Management

This document explains how version codes and version names are managed in the Angl app.

## Overview

The Angl app uses an automated version management system that ensures each release has a unique version code, which is required by Google Play Store.

## Version Code

The **version code** is an integer that must be incremented for each release uploaded to the Play Store. Google Play uses this to determine which version is newer.

### How It Works

1. **Local Development Builds**: When building locally, the version code is read from `version.properties` file.
2. **GitHub Actions CI/CD**: When building in GitHub Actions, the version code is automatically calculated as `1000 + GITHUB_RUN_NUMBER`, ensuring each CI build has a unique, incrementing version code.

### Automatic Increment in CI

The GitHub Actions workflow automatically uses the run number to generate unique version codes:
- Run #1 → Version Code 1001
- Run #2 → Version Code 1002
- Run #3 → Version Code 1003
- And so on...

This ensures that every release built in CI will have a unique version code without manual intervention.

## Version Name

The **version name** is a user-visible string that follows semantic versioning: `MAJOR.MINOR.PATCH`

Example: `1.3.0`

### Updating Version Name

To update the version name for a new release:

1. Edit the `version.properties` file
2. Update `VERSION_MAJOR`, `VERSION_MINOR`, or `VERSION_PATCH` as appropriate
3. Commit and push the changes

Example:
```properties
VERSION_MAJOR=1
VERSION_MINOR=4
VERSION_PATCH=0
```

This will result in version name `1.4.0`

## version.properties File

The `version.properties` file contains:

```properties
# Version code - only used for local builds (CI uses run number)
VERSION_CODE=4

# Version name components - used for all builds
VERSION_MAJOR=1
VERSION_MINOR=3
VERSION_PATCH=0
```

## Release Process

### For Play Store Release

1. **Update version name** (if needed) in `version.properties`
2. **Commit and push** changes to trigger GitHub Actions
3. **Download AAB** from GitHub Actions artifacts
4. **Upload to Play Store** - the version code will be unique automatically

### Manual Local Build

If you need to manually build a release:

1. **Update VERSION_CODE** in `version.properties` to be higher than any previous release
2. **Update version name** if needed
3. Run `./gradlew bundleRelease`
4. The AAB will be in `app/build/outputs/bundle/release/`

## Version Code History

- Initial releases: Version codes 1-4 (manual)
- CI/CD releases: Version codes 1000+ (automatic)

## Best Practices

1. **Never reuse a version code** - each Play Store upload must have a unique, higher version code
2. **Use semantic versioning** for version names (MAJOR.MINOR.PATCH)
3. **Let CI handle version codes** - the GitHub Actions workflow will automatically increment
4. **Only update VERSION_CODE manually** if building locally for Play Store upload
5. **Update version name** when adding features (MINOR) or fixing bugs (PATCH)

## Troubleshooting

### "Version code X has already been used"

This error means you're trying to upload a release with a version code that was already used.

**Solution for CI builds**: 
- Simply trigger a new build - GitHub Actions will automatically use a higher version code

**Solution for local builds**: 
- Increase the `VERSION_CODE` in `version.properties` to a number higher than any previously used version code

### How to check current version

The build process prints version information:
```
===========================================
Building Angl
Version Code: 1001
Version Name: 1.3.0
===========================================
```

You can also check:
- In the AAB metadata after building
- In Google Play Console (shows all uploaded version codes)
- In `version.properties` file (for the base version code)
