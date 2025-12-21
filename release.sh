#!/bin/bash

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "Usage: ./release.sh <version>"
    echo "Example: ./release.sh 1.0.0"
    exit 1
fi

echo "Building GGSCC v$VERSION..."

./gradlew clean assemblePrivilegedRelease

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

RELEASE_DIR="release_v$VERSION"
mkdir -p "$RELEASE_DIR"

APK_SOURCE="app/build/outputs/apk/privileged/release/app-privileged-release.apk"
cp "$APK_SOURCE" "$RELEASE_DIR/GGSCC_v${VERSION}.apk"

cp README.md "$RELEASE_DIR/"
cp INSTALL.md "$RELEASE_DIR/"
cp LICENSE "$RELEASE_DIR/" 2>/dev/null || true
cp privapp-permissions-com.ggscc.app.xml "$RELEASE_DIR/"

cat > "$RELEASE_DIR/install.sh" << 'EOF'
#!/bin/bash
echo "Installing GGSCC (Geely Galaxy Starship Command Center)..."
adb root
adb remount
adb shell rm -rf /system/priv-app/CarControl /system/priv-app/com.ggscc.app
adb shell mkdir -p /system/priv-app/com.ggscc.app
adb push GGSCC_v*.apk /system/priv-app/com.ggscc.app/com.ggscc.app.apk
adb push privapp-permissions-com.ggscc.app.xml /system/etc/permissions/
adb shell chmod 644 /system/priv-app/com.ggscc.app/com.ggscc.app.apk
adb shell chmod 644 /system/etc/permissions/privapp-permissions-com.ggscc.app.xml
echo "Installation complete. Rebooting..."
adb reboot
EOF
chmod +x "$RELEASE_DIR/install.sh"

zip -r "GGSCC_v$VERSION.zip" "$RELEASE_DIR"

echo ""
echo "Release created: GGSCC_v$VERSION.zip"
echo "Contents:"
ls -la "$RELEASE_DIR"

rm -rf "$RELEASE_DIR"

echo ""
echo "Done!"
