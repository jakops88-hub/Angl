# Release Signing Setup Guide

> **📖 For comprehensive signing instructions, see [SIGNING.md](SIGNING.md)**

This document provides a quick overview of the release signing setup. For detailed instructions, troubleshooting, and best practices, please refer to **SIGNING.md**.

## Quick Start

### For Local Development

1. Generate keystore: 
   ```bash
   keytool -genkey -v -keystore app/keystore.jks -alias angl-key -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Set environment variables:
   ```bash
   export SIGNING_STORE_PASSWORD="your_keystore_password"
   export SIGNING_KEY_ALIAS="angl-key"
   export SIGNING_KEY_PASSWORD="your_key_password"
   ```

3. Build:
   ```bash
   ./gradlew bundleRelease
   ```

See [SIGNING.md](SIGNING.md) for detailed instructions.

### For CI/CD (GitHub Actions)

1. Encode keystore as Base64:
   ```bash
   base64 -w 0 app/keystore.jks > keystore_base64.txt
   ```

2. Add GitHub Secrets:
   - `SIGNING_KEY_STORE_BASE64`
   - `SIGNING_KEY_ALIAS`
   - `SIGNING_KEY_PASSWORD`
   - `SIGNING_STORE_PASSWORD`

3. Workflow runs automatically on push to `main` or manual trigger

See [SIGNING.md](SIGNING.md) for detailed setup instructions and troubleshooting.

## Important Notes

- ⚠️ **Never commit keystores to git** (already in `.gitignore`)
- ⚠️ **Backup your keystore securely** - losing it means you can't update your app
- ⚠️ **Keep passwords secure** - store in password manager or vault
- ✅ **Unsigned builds are allowed** - useful for testing without Play Store upload

## Build Behavior

- **With keystore**: Signed AAB/APK (suitable for Play Store)
- **Without keystore**: Unsigned AAB/APK (not suitable for Play Store)

The build system gracefully handles missing keystores and will produce unsigned builds with warning messages.

## Documentation

For comprehensive information, see:
- **[SIGNING.md](SIGNING.md)** - Complete signing guide with troubleshooting
- **[Build Workflow](.github/workflows/build_release.yml)** - GitHub Actions configuration
