# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GGSCC (Geely Galaxy Starship Command Center) - Android application for controlling Geely/FlymeAuto vehicle functions via Android Car API. The app provides UI for climate control, seat functions, windows, and a Property Scanner for discovering available Car API properties.

**Package ID**: `com.ggscc.app`

## Build Commands

**ВАЖНО для Claude**: Всегда используй `./deploy.sh` для сборки и деплоя. Не выполняй gradle/adb команды вручную.

```bash
# Quick update (app already installed) - builds and deploys automatically
./deploy.sh

# Full reinstall (first time or reset)
./deploy.sh --fresh

# Create release package for distribution
./release.sh <version>
```

## Installation (requires ADB)

### ROOT installation (recommended)
This method installs the app as a system privileged app, bypassing the need for platform keys.

```bash
# 1. Clean up old installations
adb root && adb remount
adb shell rm -rf /system/priv-app/CarControl /system/priv-app/com.ggscc.app
adb uninstall com.ggscc.app

# 2. Setup directory structure
adb shell mkdir -p /system/priv-app/com.ggscc.app

# 3. Push APK and Set Permissions
adb push app/build/outputs/apk/privileged/release/app-privileged-release.apk /system/priv-app/com.ggscc.app/com.ggscc.app.apk
adb shell chmod 644 /system/priv-app/com.ggscc.app/com.ggscc.app.apk

# 4. Push Privileged Permissions Whitelist
adb push privapp-permissions-com.ggscc.app.xml /system/etc/permissions/
adb shell chmod 644 /system/etc/permissions/privapp-permissions-com.ggscc.app.xml

# 5. Reboot
adb reboot
```

### Launch
```bash
adb shell am start -n com.ggscc.app/.MainActivity
```

## Debugging
```bash
adb logcat | grep -E "CarModel|VehiclePropertyHelper|MainActivity|Controller"
adb shell dumpsys car_service --services CarPropertyService
```

## Discovering New Vehicle Properties

При добавлении новых функций необходимо сначала найти соответствующие property IDs через мониторинг изменений в настройках автомобиля.

### Мониторинг изменений свойств

1. **Запустить мониторинг логов**:
```bash
adb logcat -c && adb logcat | grep -i -E "property.*change|setproperty|onpropertychange|GeelyAudioImpl|VHalProperty|CLSZ|AutoFuncImpl"
```

2. **Изменить настройку в штатном UI автомобиля** (например, переключить режим звука)

3. **Проанализировать логи** - искать строки с:
   - `propertyId = XXXXXXX` - ID свойства
   - `propertyValue: X` или `value: X` - значение
   - `onChangeEvent` - события изменения
   - Названия функций (например, `SETTING_FUNC_AUDIO_SEAT_SOUND_OPTIMIZE`)

### Пример: Обнаружение аудио свойств

**Property 557872198** - Seat Sound Optimize (режим оптимизации звука):
```
GeelyAudioImpl: onChangeEvent: propertyId = 557872198
AutoFuncImpl: setEnum propertyValue: 0, funcId: SETTING_FUNC_AUDIO_SEAT_SOUND_OPTIMIZE
```
Значения:
- `0` = All Seats (весь салон)
- `1` = Driver (водитель)
- `2` = Rear (задние сиденья)
- `10` = Front Seats (передние)

**Property 555775047** - Surround Switch:
```
GeelyAudioImpl: onChangeEvent: propertyId = 555775047
GeelyAudioImpl: onChangeEvent: surroundSoundEffectSwitch: true
```
Значения: Boolean (true/false)

### Важные заметки

- Некоторые свойства взаимосвязаны (например, Surround отключает другие режимы)
- Property может иметь статус (`status: 0` = enabled, `status: 1` = disabled)
- Vendor использует внутренние значения (`func value`) которые маппятся на `propertyValue`
- Всегда тестировать все возможные состояния для полного понимания диапазона значений

## Architecture

### UI Fragment Pattern - CRITICAL RULE

**ВАЖНО**: При инициализации UI фрагментов (в методах `onViewCreated`, `onResume`, `loadCurrentState`) разрешено ТОЛЬКО ЧТЕНИЕ текущих настроек автомобиля для отображения в UI. НИКОГДА не устанавливайте значения свойств автомобиля без явного действия пользователя.

**Правильный подход**:
```kotlin
private fun loadCurrentState() {
    isLoadingState = true  // Блокируем listeners
    try {
        // ТОЛЬКО чтение и отображение
        binding.toggleDriveMode.check(getCurrentDriveMode())
        binding.switchFogLights.isChecked = getFogLightState()
    } finally {
        binding.root.post { isLoadingState = false }
    }
}

private fun setupListeners() {
    binding.toggleDriveMode.addOnButtonCheckedListener { _, _, isChecked ->
        if (isLoadingState || !isChecked) return@...  // Проверяем флаг ПЕРВЫМ
        // Только здесь разрешено изменять настройки
        controller.setDriveMode(...)
    }
}
```

**Почему это важно**:
- При смене языка Android пересоздаёт Activity
- Без защиты `isLoadingState` все UI элементы при установке значений вызывают listeners
- Это приводит к нежелательному изменению настроек автомобиля (зеркала складываются, меняется режим вождения и т.д.)

**Обязательно**:
1. В каждом фрагменте должен быть флаг `private var isLoadingState = false`
2. Все listeners должны проверять `if (isLoadingState) return@...` ПЕРВЫМ
3. Для ToggleButton: `if (isLoadingState || !isChecked) return@...`
4. Метод `loadCurrentState()` всегда оборачивает код в `isLoadingState = true/false`
5. Сброс флага через `binding.root.post { isLoadingState = false }` для асинхронности

### Core Components

**CarModel** (`car/CarModel.kt`) - Vehicle model constant (P145 - Starship).

**VehiclePropertyHelper** (`car/VehiclePropertyHelper.kt`) - Wrapper for `CarPropertyManager`. Provides typed getters/setters for INT, FLOAT, BOOLEAN properties with area support.

**PropertyConstants** (`tools/PropertyConstants.kt`) - Vehicle property IDs and area constants.

### Controllers

Each controller wraps `VehiclePropertyHelper` for a specific vehicle subsystem:
- `WindowController` - Individual windows + sunroof positioning (0-100%)
- `TrunkController` - Open/close with model-specific property IDs
- `SeatMassageController` - Power levels (OFF/LOW/MEDIUM/HIGH) + massage types
- `SeatHeatingController` - Heating levels (0-3) for driver/passenger
- `SeatVentilationController` - Seat ventilation control
- `LightController` - Interior lights by zone
- `ExteriorLightController` - Exterior lights control
- `DriveModeController` - Vehicle drive mode selection

## Key Implementation Details

- **Privileged App Mode**: App runs as a system app in `/system/priv-app/`
- Requires `useLibrary("android.car")` in build.gradle.kts
- Property IDs are vendor-specific integers (e.g., 356517131 for seat heating)
- Area IDs represent zones (1=driver, 4=passenger, 16/64/256/1024=window corners)

## Supported Vehicle

Geely Galaxy Starship 7 (P145)
