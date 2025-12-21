package com.ggscc.app.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object LocaleManager {
    private const val PREF_NAME = "car_control_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun getCurrentLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, getDefaultLanguage()) ?: "en"
    }

    fun setLanguage(context: Context, language: String) {
        getPrefs(context).edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun getDefaultLanguage(): String {
        val systemLang = Locale.getDefault().language
        return if (systemLang == "ru") "ru" else "en"
    }

    fun applyLocale(activity: Activity) {
        val language = getCurrentLanguage(activity)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = activity.resources.configuration
        config.setLocale(locale)
        activity.createConfigurationContext(config)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
