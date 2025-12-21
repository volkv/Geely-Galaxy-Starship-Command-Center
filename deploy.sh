#!/bin/bash
set -e

PACKAGE_NAME="com.ggscc.app"
APK_PATH="app/build/outputs/apk/privileged/release/app-privileged-release.apk"
SYSTEM_APK_PATH="/system/priv-app/$PACKAGE_NAME/$PACKAGE_NAME.apk"

if [ ! -f "keys/platform.p12" ]; then
    echo "Ключи не найдены. Запускаю setup-keys.sh..."
    ./setup-keys.sh
fi

./gradlew assemblePrivilegedRelease

adb root
sleep 2

if [ "$1" == "--fresh" ]; then
    adb remount
    adb shell rm -rf /system/priv-app/CarControl /system/priv-app/$PACKAGE_NAME
    adb shell mkdir -p /system/priv-app/$PACKAGE_NAME
    adb push "$APK_PATH" $SYSTEM_APK_PATH
    adb push privapp-permissions-$PACKAGE_NAME.xml /system/etc/permissions/
    adb shell chmod 644 $SYSTEM_APK_PATH
    adb shell chmod 644 /system/etc/permissions/privapp-permissions-$PACKAGE_NAME.xml
    adb reboot
    echo "GGSCC установлен. Ожидание перезагрузки..."
else
    adb remount
    adb shell am force-stop $PACKAGE_NAME
    adb push "$APK_PATH" $SYSTEM_APK_PATH
    adb shell chmod 644 $SYSTEM_APK_PATH
    adb shell pm install -r -d $SYSTEM_APK_PATH
    adb shell am start -n $PACKAGE_NAME/.MainActivity
    echo "GGSCC обновлён (privileged mode сохранён)."
fi
