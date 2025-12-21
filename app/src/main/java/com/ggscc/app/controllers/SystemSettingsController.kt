package com.ggscc.app.controllers

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast

class SystemSettingsController(private val context: Context) {
    companion object {
        private const val TAG = "SystemSettingsController"
    }

    data class Language(val code: String, val displayName: String, val locale: String)
    data class TimeZone(val id: String, val displayName: String, val offset: String)

    val availableLanguages = listOf(
        Language("ru", "Русский", "ru-US"),
        Language("en", "English", "en-US"),
        Language("zh", "中文", "zh-US")
    )

    val availableTimeZones = listOf(
        TimeZone("Europe/Moscow", "Москва (МСК)", "UTC+3"),
        TimeZone("Asia/Yekaterinburg", "Екатеринбург", "UTC+5"),
        TimeZone("Asia/Novosibirsk", "Новосибирск", "UTC+7"),
        TimeZone("Asia/Vladivostok", "Владивосток", "UTC+10"),
        TimeZone("Asia/Shanghai", "Китай (Пекин)", "UTC+8")
    )

    fun setSystemLanguage(languageCode: String): Boolean {
        return try {
            val locale = availableLanguages.find { it.code == languageCode }?.locale ?: return false
            val result = setSystemLocale(locale)
            if (result) {
                Toast.makeText(context, "Язык изменен (разделитель: точка)", Toast.LENGTH_LONG).show()
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change system language", e)
            Toast.makeText(context, "Требуются системные привилегии", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun setSystemLocale(localeTag: String): Boolean {
        return try {
            val activityManagerClass = Class.forName("android.app.IActivityManager")
            val activityManagerStubClass = Class.forName("android.app.IActivityManager\$Stub")
            val serviceManagerClass = Class.forName("android.os.ServiceManager")

            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val service = getServiceMethod.invoke(null, Context.ACTIVITY_SERVICE)

            val asInterfaceMethod = activityManagerStubClass.getMethod("asInterface", android.os.IBinder::class.java)
            val activityManager = asInterfaceMethod.invoke(null, service)

            val configClass = Class.forName("android.content.res.Configuration")
            val config = configClass.newInstance()

            val localeClass = Class.forName("java.util.Locale")
            val forLanguageTagMethod = localeClass.getMethod("forLanguageTag", String::class.java)
            val localeObj = forLanguageTagMethod.invoke(null, localeTag)

            val setLocaleMethod = configClass.getMethod("setLocale", localeClass)
            setLocaleMethod.invoke(config, localeObj)

            val updateConfigMethod = activityManagerClass.getMethod("updatePersistentConfiguration", configClass)
            updateConfigMethod.invoke(activityManager, config)

            Log.i(TAG, "System locale changed to: $localeTag")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change system locale", e)
            false
        }
    }

    fun setSystemTimeZone(timeZoneId: String): Boolean {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val setTimeZoneMethod = alarmManager.javaClass.getMethod("setTimeZone", String::class.java)
            setTimeZoneMethod.invoke(alarmManager, timeZoneId)

            Log.i(TAG, "System timezone changed to: $timeZoneId")
            Toast.makeText(context, "Часовой пояс изменен", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change timezone", e)
            Toast.makeText(context, "Ошибка смены часового пояса", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun getCurrentTimeZone(): String {
        return try {
            java.util.TimeZone.getDefault().id
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getCurrentLanguage(): String {
        return try {
            val locale = context.resources.configuration.locales[0]
            locale.language
        } catch (e: Exception) {
            "en"
        }
    }

    private fun launchSettings(action: String, logName: String, errorMessage: String? = null): Boolean {
        return try {
            context.startActivity(Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            Log.i(TAG, "Opened $logName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open $logName", e)
            errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
            false
        }
    }

    private fun launchSettingsWithFallback(
        actions: List<String>,
        logName: String,
        errorMessage: String
    ): Boolean {
        for (action in actions) {
            try {
                context.startActivity(Intent(action).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                Log.i(TAG, "Opened $logName with action: $action")
                return true
            } catch (_: Exception) {
                continue
            }
        }
        Log.e(TAG, "Failed to open $logName")
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun launchByComponent(
        candidates: List<Pair<String, String>>,
        logName: String,
        errorMessage: String
    ): Boolean {
        for ((pkg, cls) in candidates) {
            try {
                context.startActivity(Intent().apply {
                    component = android.content.ComponentName(pkg, cls)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                Log.i(TAG, "Opened $logName: $pkg")
                return true
            } catch (_: Exception) {
                context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                    context.startActivity(it)
                    Log.i(TAG, "Opened $logName via launcher: $pkg")
                    return true
                }
            }
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        return false
    }

    fun openGeelyEngineeringMenu() = launchByComponent(
        listOf(
            "com.geely.engineermode" to "com.geely.engineermode.MainActivity",
            "ecarx.debugtools" to "ecarx.debugtools.MainActivity",
            "com.mediatek.engineermode" to "com.mediatek.engineermode.MainActivity"
        ),
        "Geely Engineering Menu",
        "Инженерное меню не найдено"
    )

    fun openDeveloperOptions() = launchSettings(
        Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
        "Developer Options",
        "Настройки разработчика недоступны"
    )

    fun openLanguageSettings() = launchSettingsWithFallback(
        listOf(
            "android.settings.LOCALE_SETTINGS",
            "android.settings.LANGUAGE_SETTINGS",
            "com.android.settings.LANGUAGE_AND_INPUT_SETTINGS",
            Settings.ACTION_LOCALE_SETTINGS
        ),
        "Language Settings",
        "Настройки языка недоступны"
    )

    fun openDateTimeSettings() = launchSettings(Settings.ACTION_DATE_SETTINGS, "Date & Time Settings")

    fun openWifiSettings() = launchSettings(
        Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings", "Не удалось открыть Wi-Fi настройки"
    )

    fun openBluetoothSettings() = launchSettings(
        Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings", "Не удалось открыть Bluetooth настройки"
    )

    fun openDisplaySettings() = launchSettings(
        Settings.ACTION_DISPLAY_SETTINGS, "Display Settings", "Не удалось открыть настройки дисплея"
    )

    fun openSoundSettings() = launchSettings(
        Settings.ACTION_SOUND_SETTINGS, "Sound Settings", "Не удалось открыть настройки звука"
    )

    fun openAppsSettings() = launchSettings(
        Settings.ACTION_APPLICATION_SETTINGS, "Apps Settings", "Не удалось открыть настройки приложений"
    )

    fun openStorageSettings() = launchSettingsWithFallback(
        listOf(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, Settings.ACTION_MEMORY_CARD_SETTINGS),
        "Storage Settings",
        "Не удалось открыть настройки хранилища"
    )

    fun openSecuritySettings() = launchSettings(
        Settings.ACTION_SECURITY_SETTINGS, "Security Settings", "Не удалось открыть настройки безопасности"
    )

    fun openInputMethodSettings() = launchSettings(
        Settings.ACTION_INPUT_METHOD_SETTINGS, "Input Method Settings", "Не удалось открыть настройки ввода"
    )
}
