package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Hud

class HudController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "HudController"
    }

    fun enable(): Boolean {
        Log.i(TAG, "Enabling HUD")
        return vehicleHelper.setBoolProperty(Hud.ACTIVE_PROPERTY_ID, Hud.AREA, true)
    }

    fun disable(): Boolean {
        Log.i(TAG, "Disabling HUD")
        return vehicleHelper.setBoolProperty(Hud.ACTIVE_PROPERTY_ID, Hud.AREA, false)
    }

    fun isEnabled(): Boolean {
        return vehicleHelper.getBoolProperty(Hud.ACTIVE_PROPERTY_ID, Hud.AREA)
    }

    fun toggle(): Boolean {
        val current = isEnabled()
        return if (current) disable() else enable()
    }
}
