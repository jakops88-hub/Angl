# Android App Signing Guide for Google Play Store

This guide provides comprehensive instructions for setting up release signing for the Angl Android application.

## Table of Contents
- [Overview](#overview)
- [Local Development Setup](#local-development-setup)
- [CI/CD Setup (GitHub Actions)](#cicd-setup-github-actions)
- [Security Best Practices](#security-best-practices)
- [Troubleshooting](#troubleshooting)

---

## Overview

For publishing to Google Play Store, your app must be signed with a **release keystore**. This keystore must:
- Be kept secure and never committed to version control
- Use the same signing key for all updates to your app
- Be backed up securely (losing it means you can't update your app)

### Current Build Behavior

The build system is designed to handle both signed and unsigned builds:

- **With keystore**: Creates a signed AAB/APK suitable for Play Store upload
- **Without keystore**: Creates an unsigned AAB/APK (cannot be uploaded to Play Store)

---

## Local Development Setup

### Step 1: Generate Your Production Keystore

Run this command in your terminal:

```bash
keytool -genkey -v -keystore app/keystore.jks -alias angl-key -keyalg RSA -keysize 3072 -validity 10000
```

**Parameters explained:**
- `-keystore app/keystore.jks`: Output file location
- `-alias angl-key`: Identifier for your signing key (remember this!)
- `-keyalg RSA -keysize 3072`: Cryptographic algorithm and key size (3072-bit for enhanced security)
- `-validity 10000`: Key valid for ~27 years

### Step 2: Provide Required Information

When prompted, enter:

| Prompt | What to Enter | Example |
|--------|---------------|---------|
| Keystore password | Strong password (min 6 chars) | `MyStr0ng!KeystorePwd` |
| Re-enter password | Same as above | `MyStr0ng!KeystorePwd` |
| First and last name | Your/Organization name | `Angl Development Team` |
| Organizational unit | Your team/department | `Mobile Development` |
| Organization | Company name | `Angl Inc` |
| City/Locality | Your city | `San Francisco` |
| State/Province | Your state | `California` |
| Country code | Two-letter code | `US` |
| Key password | Strong password (can differ) | `MyStr0ng!KeyPwd` |

**⚠️ CRITICAL**: Write down these passwords and store them securely!

### Step 3: Set Environment Variables

Before building, set these environment variables:

**Linux/macOS:**
```bash
export SIGNING_STORE_PASSWORD="your_keystore_password"
export SIGNING_KEY_ALIAS="angl-key"
export SIGNING_KEY_PASSWORD="your_key_password"
```

**Windows (Command Prompt):**
```cmd
set SIGNING_STORE_PASSWORD=your_keystore_password
set SIGNING_KEY_ALIAS=angl-key
set SIGNING_KEY_PASSWORD=your_key_password
```

**Windows (PowerShell):**
```powershell
$env:SIGNING_STORE_PASSWORD="your_keystore_password"
$env:SIGNING_KEY_ALIAS="angl-key"
$env:SIGNING_KEY_PASSWORD="your_key_password"
```

### Step 4: Build Signed Release

```bash
./gradlew bundleRelease  # For AAB (Play Store)
./gradlew assembleRelease  # For APK
```

Find your signed artifacts:
- **AAB**: `app/build/outputs/bundle/release/app-release.aab`
- **APK**: `app/build/outputs/apk/release/app-release.apk`

---

## CI/CD Setup (GitHub Actions)

### Step 1: Encode Keystore as Base64

**Linux/macOS:**
```bash
base64 -w 0 app/keystore.jks > keystore_base64.txt
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app/keystore.jks")) | Out-File -FilePath keystore_base64.txt -NoNewline
```

**Windows (Git Bash):**
```bash
base64 -w 0 app/keystore.jks > keystore_base64.txt
```

### Step 2: Add GitHub Secrets

1. Go to your repository on GitHub
2. Navigate to: **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each of these secrets:

| Secret Name | Value | Notes |
|-------------|-------|-------|
| `SIGNING_KEY_STORE_BASE64` | Contents of `keystore_base64.txt` | Entire base64 string, no line breaks |
| `SIGNING_KEY_ALIAS` | `angl-key` | Must match alias used in keytool command |
| `SIGNING_KEY_PASSWORD` | Your key password | Password for the signing key |
| `SIGNING_STORE_PASSWORD` | Your keystore password | Password for the keystore file |

**Important**: Ensure there are no extra spaces or line breaks in the secret values!

### Step 3: Trigger GitHub Actions Build

The workflow runs automatically when:
- Code is pushed to `main` branch
- Manually triggered from Actions tab

**To manually trigger:**
1. Go to **Actions** tab
2. Select **Build Release AAB** workflow
3. Click **Run workflow**
4. Select branch and click **Run workflow**

### Step 4: Download Build Artifacts

1. Go to **Actions** tab
2. Click on the completed workflow run
3. Scroll to **Artifacts** section
4. Download **app-release** (contains the AAB file)

---

## Security Best Practices

### DO ✅

- **Keep keystore secure**: Store in password manager or secure vault
- **Backup keystore**: Keep multiple secure backups (encrypted cloud storage, safe deposit box)
- **Use strong passwords**: Minimum 12 characters with mixed case, numbers, symbols
- **Rotate secrets**: If compromised, immediately create new keystore for new apps
- **Limit access**: Only trusted team members should have keystore access
- **Use GitHub Secrets**: For CI/CD, never hardcode credentials

### DON'T ❌

- **Never commit keystore** to version control (.gitignore prevents this)
- **Never share passwords** via email, chat, or insecure channels
- **Never reuse passwords** from other services
- **Never upload keystore** to public cloud storage without encryption
- **Never lose your keystore**: You cannot recover it or update your app without it

### .gitignore Protection

The `.gitignore` file includes:
```
# Keystore files
*.jks
*.keystore
```

This prevents accidental commits. If you accidentally committed a keystore:
1. Remove it immediately: `git rm --cached app/keystore.jks`
2. Generate a NEW keystore (the old one is compromised)
3. Update all secrets with new keystore

---

## Troubleshooting

### Build fails: "Keystore not found"

**Symptom**: Warning messages about unsigned build

**Local Development:**
- Verify `app/keystore.jks` exists
- Check file permissions (should be readable)

**CI/CD:**
- Verify `SIGNING_KEY_STORE_BASE64` secret is set
- Check base64 encoding is correct (no line breaks)
- Ensure secret value has no leading/trailing spaces

### Build fails: "Wrong password"

**Symptom**: Authentication error during signing

**Solution:**
- Verify `SIGNING_STORE_PASSWORD` matches keystore password
- Verify `SIGNING_KEY_PASSWORD` matches key password
- Check for typos or extra spaces in environment variables/secrets
- Ensure passwords don't contain special shell characters that need escaping

### Build fails: "Alias not found"

**Symptom**: Cannot find signing key with specified alias

**Solution:**
- Verify `SIGNING_KEY_ALIAS` matches the alias used in keytool command
- List aliases in keystore: `keytool -list -keystore app/keystore.jks`
- Update environment variable/secret with correct alias

### Build fails: "Tag number over 30 is not supported"

**Symptom**: Corrupted or invalid keystore file

**Possible Causes:**
1. Keystore file is corrupted
2. Base64 decoding failed (CI/CD)
3. Empty or invalid secret value
4. Wrong keystore format

**Solutions:**
- **Local**: Regenerate keystore using keytool command above
- **CI/CD**: Re-encode keystore and update `SIGNING_KEY_STORE_BASE64` secret
- Verify keystore is valid: `keytool -list -keystore app/keystore.jks`

### Build succeeds but app won't install

**Symptom**: "Package conflicts with existing package" error

**Solution:**
- Uninstall existing debug builds before installing release build
- Ensure applicationId matches between debug/release builds
- Check signing configuration in build.gradle.kts

### Play Store rejects upload

**Symptom**: "Upload failed: You uploaded an APK that is not signed"

**Solution:**
- Verify build logs show "Keystore file exists - building SIGNED release"
- Check that AAB/APK is signed: `jarsigner -verify -verbose -certs app-release.aab`
- Ensure you're uploading the release build, not debug

---

## Play Store Upload Process

### First-Time Upload

1. Go to [Google Play Console](https://play.google.com/console)
2. Create a new app or select existing
3. Complete store listing (title, description, screenshots, etc.)
4. Go to **Release** → **Production** (or Testing track)
5. Click **Create new release**
6. Upload `app-release.aab`
7. Complete release information
8. Review and roll out

### Update Existing App

**⚠️ CRITICAL**: You MUST use the SAME keystore as the original release!

1. Increment `versionCode` and `versionName` in `app/build.gradle.kts`
2. Build new signed release
3. Go to Play Console → **Release** → **Production**
4. Click **Create new release**
5. Upload new `app-release.aab`
6. Add release notes
7. Review and roll out

---

## Additional Resources

- [Android Developer Guide: Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [Managing App Signing Keys](https://developer.android.com/studio/publish/app-signing#secure-keys)

---

## Quick Reference

### Commands Cheatsheet

```bash
# Generate keystore
keytool -genkey -v -keystore app/keystore.jks -alias angl-key -keyalg RSA -keysize 3072 -validity 10000

# List keystore contents
keytool -list -keystore app/keystore.jks

# Verify signed AAB
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab

# Build signed release (local)
./gradlew bundleRelease  # AAB
./gradlew assembleRelease  # APK

# Build debug (no signing needed)
./gradlew bundleDebug
./gradlew assembleDebug
```

### Environment Variables

```bash
SIGNING_STORE_PASSWORD=<keystore_password>
SIGNING_KEY_ALIAS=<key_alias>
SIGNING_KEY_PASSWORD=<key_password>
```

---

**Need Help?** Check the GitHub Actions workflow logs for detailed error messages (navigate to the Actions tab in your repository).
