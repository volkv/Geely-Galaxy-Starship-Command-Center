package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.ExteriorLight

class ExteriorLightController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "ExteriorLightController"
    }

    fun getLightState(): Int {
        val state = vehicleHelper.getIntProperty(ExteriorLight.PROPERTY_ID, ExteriorLight.AREA)
        Log.d(TAG, "Current light state: $state")
        return state
    }

    fun setLightState(state: Int): Boolean {
        val stateName = when (state) {
            ExteriorLight.OFF -> "OFF"
            ExteriorLight.PARKING -> "PARKING"
            ExteriorLight.HEADLIGHTS -> "HEADLIGHTS"
            else -> "UNKNOWN"
        }
        Log.i(TAG, "Setting exterior lights to $stateName ($state)")
        return vehicleHelper.setIntProperty(ExteriorLight.PROPERTY_ID, ExteriorLight.AREA, state)
    }

    fun turnOff(): Boolean = setLightState(ExteriorLight.OFF)
    fun setParkingLights(): Boolean = setLightState(ExteriorLight.PARKING)
    fun setHeadlights(): Boolean = setLightState(ExteriorLight.HEADLIGHTS)
}
