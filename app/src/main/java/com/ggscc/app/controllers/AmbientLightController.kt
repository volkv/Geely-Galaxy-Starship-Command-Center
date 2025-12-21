package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.AmbientLight

class AmbientLightController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "AmbientLightController"
    }

    fun enable(): Boolean {
        Log.i(TAG, "Enabling ambient light")
        return vehicleHelper.setBoolProperty(
            AmbientLight.SWITCH_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            true
        )
    }

    fun disable(): Boolean {
        Log.i(TAG, "Disabling ambient light")
        return vehicleHelper.setBoolProperty(
            AmbientLight.SWITCH_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            false
        )
    }

    fun isEnabled(): Boolean {
        return vehicleHelper.getBoolProperty(
            AmbientLight.SWITCH_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL
        )
    }

    fun setIntensity(level: Int): Boolean {
        val clampedLevel = level.coerceIn(AmbientLight.INTENSITY_MIN, AmbientLight.INTENSITY_MAX)
        Log.i(TAG, "Setting ambient light intensity to $clampedLevel")
        return vehicleHelper.setIntProperty(
            AmbientLight.INTENSITY_PROPERTY_ID,
            AmbientLight.AREA_FRONT_ROW,
            clampedLevel
        )
    }

    fun getIntensity(): Int {
        return vehicleHelper.getIntProperty(
            AmbientLight.INTENSITY_PROPERTY_ID,
            AmbientLight.AREA_FRONT_ROW
        )
    }

    fun enableBreatheMode(color: Int): Boolean {
        Log.i(TAG, "Enabling breathe mode with color ${color.toString(16)}")
        val disableTransition = vehicleHelper.setBoolProperty(
            AmbientLight.TRANSITION_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            false
        )
        val enableBreathe = vehicleHelper.setBoolProperty(
            AmbientLight.BREATHE_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            true
        )
        val setColor = vehicleHelper.setIntProperty(
            AmbientLight.BREATHE_COLOR_PROPERTY_ID,
            AmbientLight.AREA_FRONT_ROW,
            color
        )
        return disableTransition && enableBreathe && setColor
    }

    fun enableGradientMode(endColor: Int): Boolean {
        Log.i(TAG, "Enabling gradient mode with end color ${endColor.toString(16)}")
        val disableBreathe = vehicleHelper.setBoolProperty(
            AmbientLight.BREATHE_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            false
        )
        val enableTransition = vehicleHelper.setBoolProperty(
            AmbientLight.TRANSITION_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            true
        )
        val setEndColor = vehicleHelper.setIntProperty(
            AmbientLight.TRANSITION_END_COLOR_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            endColor
        )
        return disableBreathe && enableTransition && setEndColor
    }

    fun disableEffects(): Boolean {
        Log.i(TAG, "Disabling all ambient light effects")
        val disableBreathe = vehicleHelper.setBoolProperty(
            AmbientLight.BREATHE_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            false
        )
        val disableTransition = vehicleHelper.setBoolProperty(
            AmbientLight.TRANSITION_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            false
        )
        return disableBreathe && disableTransition
    }

    fun isBreatheEnabled(): Boolean {
        return vehicleHelper.getBoolProperty(
            AmbientLight.BREATHE_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL
        )
    }

    fun isGradientEnabled(): Boolean {
        return vehicleHelper.getBoolProperty(
            AmbientLight.TRANSITION_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL
        )
    }

    fun getMainColorMode(): Int {
        val value = vehicleHelper.getIntProperty(
            AmbientLight.MAINCOLOR_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL
        )
        Log.i(TAG, "Current MAINCOLOR mode: $value")
        return value
    }

    fun getMusicShowMode(): Int {
        val value = vehicleHelper.getIntProperty(
            AmbientLight.MUSIC_SHOW_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL
        )
        Log.i(TAG, "Current MUSIC_SHOW_MODE: $value")
        return value
    }

    fun getMusicShowModeInner(): Int {
        val value = vehicleHelper.getIntProperty(
            AmbientLight.MUSIC_SHOW_MODE_INNER_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL
        )
        Log.i(TAG, "Current MUSIC_SHOW_MODE_INNER: $value")
        return value
    }

    fun enableMusicMode(): Boolean {
        Log.i(TAG, "Enabling music mode (color music)")
        disableEffects()
        return vehicleHelper.setIntProperty(
            AmbientLight.MAINCOLOR_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            AmbientLight.MAINCOLOR_MUSIC
        )
    }

    fun setMainColorMode(mode: Int): Boolean {
        Log.i(TAG, "Setting MAINCOLOR mode to $mode")
        return vehicleHelper.setIntProperty(
            AmbientLight.MAINCOLOR_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            mode
        )
    }

    fun setMusicShowMode(mode: Int): Boolean {
        Log.i(TAG, "Setting MUSIC_SHOW_MODE to $mode")
        return vehicleHelper.setIntProperty(
            AmbientLight.MUSIC_SHOW_MODE_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            mode
        )
    }

    fun setMusicShowModeInner(mode: Int): Boolean {
        Log.i(TAG, "Setting MUSIC_SHOW_MODE_INNER to $mode")
        return vehicleHelper.setIntProperty(
            AmbientLight.MUSIC_SHOW_MODE_INNER_PROPERTY_ID,
            AmbientLight.AREA_GLOBAL,
            mode
        )
    }

    fun activateColorMusic(): Boolean {
        Log.i(TAG, "Activating color music mode")
        val result = setMusicShowModeInner(1)
        Log.i(TAG, "activateColorMusic result: $result")
        return result
    }

    fun deactivateColorMusic(): Boolean {
        Log.i(TAG, "Deactivating color music mode")
        return setMusicShowModeInner(0)
    }
}
