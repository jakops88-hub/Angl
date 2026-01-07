# Release Signing Setup Guide

This guide explains how to set up release signing for the Angl Android app using GitHub Actions.

## Overview

The GitHub Actions workflow (`.github/workflows/build_release.yml`) automatically builds a signed Android App Bundle (.aab) when you push to the `main` branch or manually trigger it from the Actions tab.

## Step 1: Generate a Keystore

Run the following command in a terminal (or Cloud Shell) to generate a `keystore.jks` file:

```bash
keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
```

When prompted, you'll need to provide:
- **Keystore password**: Create a strong password (you'll need this for `SIGNING_STORE_PASSWORD`)
- **Key password**: Create a strong password (you'll need this for `SIGNING_KEY_PASSWORD`)
- **First and last name**: Your name or organization name
- **Organizational unit**: Your department or team name
- **Organization**: Your company name
- **City/Locality**: Your city
- **State/Province**: Your state or province
- **Country code**: Two-letter country code (e.g., US)

**Important**: Keep your keystore file and passwords secure! If you lose them, you won't be able to update your app on Google Play.

## Step 2: Encode the Keystore as Base64

After generating the keystore, encode it as Base64:

**On Linux/macOS:**
```bash
base64 -w 0 keystore.jks > keystore_base64.txt
```

**On Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks")) > keystore_base64.txt
```

The contents of `keystore_base64.txt` will be used for the `SIGNING_KEY_STORE_BASE64` secret.

## Step 3: Add GitHub Secrets

Go to your repository on GitHub → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add the following secrets:

| Secret Name | Description | Example Value |
|------------|-------------|---------------|
| `SIGNING_KEY_STORE_BASE64` | Base64-encoded keystore file | (contents of keystore_base64.txt) |
| `SIGNING_KEY_ALIAS` | Alias for the signing key | `release-key` |
| `SIGNING_KEY_PASSWORD` | Password for the key | (the key password you created) |
| `SIGNING_STORE_PASSWORD` | Password for the keystore | (the keystore password you created) |

## Step 4: Trigger the Build

You can trigger the build in two ways:

1. **Automatic**: Push code to the `main` branch
2. **Manual**: Go to **Actions** tab → **Build Release AAB** → **Run workflow**

## Step 5: Download the AAB

1. Go to the **Actions** tab in your repository
2. Click on the completed workflow run
3. Scroll down to **Artifacts**
4. Download **app-release** (the .aab file will be inside)

## Uploading to Google Play Console

1. Log in to [Google Play Console](https://play.google.com/console)
2. Select your app (or create a new one)
3. Go to **Release** → **Production** (or Testing track)
4. Click **Create new release**
5. Upload the `app-release.aab` file
6. Complete the release information and submit

## Security Best Practices

- **Never commit** your keystore file or passwords to the repository
- Store a backup of your keystore file in a secure location
- Use strong, unique passwords for both the keystore and key
- The `.gitignore` file should include `*.jks` to prevent accidental commits

## Troubleshooting

### Build fails with "keystore not found"
- Ensure `SIGNING_KEY_STORE_BASE64` secret is set correctly
- The Base64 string should be on a single line with no spaces

### Build fails with "wrong password"
- Double-check `SIGNING_STORE_PASSWORD` and `SIGNING_KEY_PASSWORD` secrets
- Ensure there are no extra spaces or newlines in the secret values

### Build fails with "alias not found"
- Verify `SIGNING_KEY_ALIAS` matches the alias used when generating the keystore
- The default alias in the keytool command above is `release-key`
