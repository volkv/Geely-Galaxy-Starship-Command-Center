package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants

class FogLightController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "FogLightController"
    }

    fun enable(): Boolean {
        Log.i(TAG, "Enabling rear fog lights")
        return vehicleHelper.setIntProperty(PropertyConstants.FogLight.PROPERTY_ID, PropertyConstants.FogLight.AREA, 1)
    }

    fun disable(): Boolean {
        Log.i(TAG, "Disabling rear fog lights")
        return vehicleHelper.setIntProperty(PropertyConstants.FogLight.PROPERTY_ID, PropertyConstants.FogLight.AREA, 0)
    }

    fun isEnabled(): Boolean {
        val value = vehicleHelper.getIntProperty(PropertyConstants.FogLight.PROPERTY_ID, PropertyConstants.FogLight.AREA)
        return value == 1
    }

    fun toggle(): Boolean {
        return if (isEnabled()) disable() else enable()
    }
}
