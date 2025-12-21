# Installation Guide for GGSCC

## Quick Start

```bash
# First-time installation
./deploy.sh --fresh

# Updates (after initial installation)
./deploy.sh
```

## Option 1: ROOT Installation (Recommended)

This method provides full access to all Car API functions.

### Requirements

- Device with ROOT access
- ADB installed on your computer
- USB debugging enabled on the device

### Automatic Installation

```bash
./deploy.sh --fresh
```

### Manual Installation

1. **Build the APK:**
```bash
./gradlew assemblePrivilegedRelease
```

2. **Install to system:**
```bash
adb root && adb remount
adb shell mkdir -p /system/priv-app/com.ggscc.app
adb push app/build/outputs/apk/privileged/release/app-privileged-release.apk \
    /system/priv-app/com.ggscc.app/com.ggscc.app.apk
adb push privapp-permissions-com.ggscc.app.xml /system/etc/permissions/
adb shell chmod 644 /system/priv-app/com.ggscc.app/com.ggscc.app.apk
adb shell chmod 644 /system/etc/permissions/privapp-permissions-com.ggscc.app.xml
adb reboot
```

### Updating the App

```bash
./deploy.sh
```

Or manually:
```bash
adb root
adb install --user 0 app/build/outputs/apk/privileged/release/app-privileged-release.apk
```

### Uninstallation

```bash
adb root && adb remount
adb shell rm -rf /system/priv-app/com.ggscc.app
adb shell rm /system/etc/permissions/privapp-permissions-com.ggscc.app.xml
adb reboot
```

---

## Option 2: Non-ROOT Installation

This method works on devices without ROOT, but some features may be unavailable.

### Installation

```bash
./gradlew assemblePrivilegedRelease
adb install -r app/build/outputs/apk/privileged/release/app-privileged-release.apk
```

Or via file manager:
```bash
adb push app/build/outputs/apk/privileged/release/app-privileged-release.apk /sdcard/Download/
```
Then install the APK through the device's file manager.

### Limitations

- Some vendor-specific properties may be inaccessible
- Features requiring system privileges may not work
- Property Scanner may show fewer properties

---

## Verification

### Check Logs

```bash
adb logcat | grep -E "MainActivity|CarModel|VehiclePropertyHelper"
```

Expected output:
```
I/CarModel: Product: name=P145, model=...
I/CarModel: Detected car model: STARSHIP
I/VehiclePropertyHelper: Car API connected: true
I/MainActivity: Connected to car: STARSHIP
```

### Test Functionality

1. Launch the application
2. Status should show "Connected to [MODEL]"
3. Try toggling AC on/off
4. Check logs for any errors

---

## Troubleshooting

### Car API Unavailable

**Symptoms:** Status shows "Car API connection error"

**Solutions:**
1. Ensure `useLibrary("android.car")` is present in build.gradle.kts
2. Verify `<uses-library android:name="android.car"/>` in AndroidManifest.xml
3. Try ROOT installation

### Properties Unavailable

**Symptoms:** Buttons don't work, logs show "Property X not available"

**Solutions:**
1. Check permissions in AndroidManifest.xml
2. Use ROOT installation
3. Run Property Scanner to see available properties

### Massage Not Working

**Symptoms:** Massage buttons have no effect

**Solutions:**
1. Verify property IDs are correct for your model
2. Check if your vehicle has massage feature in its configuration
3. Review logs for errors

### App Crashes on Launch

**Symptoms:** Application crashes immediately

**Solutions:**
1. Check logs: `adb logcat | grep -E "AndroidRuntime|FATAL"`
2. Ensure device supports Android Automotive
3. Try reinstalling the application

---

## ADB Commands

### Useful Commands

```bash
# View app logs
adb logcat | grep -i "carcontrol"

# Force stop
adb shell am force-stop com.ggscc.app

# Launch app
adb shell am start -n com.ggscc.app/.MainActivity

# Clear app data
adb shell pm clear com.ggscc.app

# List installed packages
adb shell pm list packages | grep carcontrol

# App info
adb shell dumpsys package com.ggscc.app
```

### Car Service Debugging

```bash
# Dump Car Service
adb shell dumpsys car_service

# Only Car Property Service
adb shell dumpsys car_service --services CarPropertyService

# Car API version
adb shell getprop ro.build.version.car
```
