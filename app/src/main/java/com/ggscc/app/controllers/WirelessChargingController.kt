package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.WirelessCharging

class WirelessChargingController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "WirelessChargingController"
    }

    fun enable(): Boolean {
        Log.i(TAG, "Enabling wireless charging")
        return vehicleHelper.setBoolProperty(WirelessCharging.PROPERTY_ID, WirelessCharging.AREA, true)
    }

    fun disable(): Boolean {
        Log.i(TAG, "Disabling wireless charging")
        return vehicleHelper.setBoolProperty(WirelessCharging.PROPERTY_ID, WirelessCharging.AREA, false)
    }

    fun isEnabled(): Boolean {
        return vehicleHelper.getBoolProperty(WirelessCharging.PROPERTY_ID, WirelessCharging.AREA)
    }

    fun toggle(): Boolean {
        val current = isEnabled()
        return if (current) disable() else enable()
    }
}
