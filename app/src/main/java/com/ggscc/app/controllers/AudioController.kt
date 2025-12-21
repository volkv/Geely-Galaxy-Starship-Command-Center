package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Audio

class AudioController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "AudioController"
    }

    fun getSeatSoundOptimize(): Int {
        val value = vehicleHelper.getIntProperty(
            Audio.SEAT_SOUND_OPTIMIZE_PROPERTY_ID,
            Audio.AREA_GLOBAL
        )
        Log.d(TAG, "Current seat sound optimize: $value")
        return value
    }

    fun setSeatSoundOptimize(mode: Int): Boolean {
        val modeName = when (mode) {
            Audio.OPTIMIZE_ALL_SEATS -> "ALL_SEATS"
            Audio.OPTIMIZE_FRONT_SEATS -> "FRONT_SEATS"
            Audio.OPTIMIZE_DRIVER_ONLY -> "DRIVER_ONLY"
            else -> "UNKNOWN"
        }
        Log.i(TAG, "Setting seat sound optimize to $modeName ($mode)")
        return vehicleHelper.setIntProperty(
            Audio.SEAT_SOUND_OPTIMIZE_PROPERTY_ID,
            Audio.AREA_GLOBAL,
            mode
        )
    }

    fun getSurroundSwitch(): Boolean {
        val value = vehicleHelper.getBoolProperty(
            Audio.SURROUND_SWITCH_PROPERTY_ID,
            Audio.AREA_GLOBAL
        )
        Log.d(TAG, "Surround switch: $value")
        return value
    }

    fun setSurroundSwitch(enabled: Boolean): Boolean {
        Log.i(TAG, "Setting Surround switch to $enabled")
        return vehicleHelper.setBoolProperty(
            Audio.SURROUND_SWITCH_PROPERTY_ID,
            Audio.AREA_GLOBAL,
            enabled
        )
    }
}
