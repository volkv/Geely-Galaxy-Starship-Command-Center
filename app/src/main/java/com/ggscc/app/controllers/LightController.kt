package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Light

class LightController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "LightController"
    }

    private val propertyId = Light.PROPERTY_ID

    fun turnOnInteriorLight(areaId: Int = Light.AREA_1): Boolean {
        Log.i(TAG, "Turning on interior light, area=$areaId")
        return vehicleHelper.setIntProperty(propertyId, areaId, Light.STATE_ON)
    }

    fun turnOffInteriorLight(areaId: Int = Light.AREA_1): Boolean {
        Log.i(TAG, "Turning off interior light, area=$areaId")
        return vehicleHelper.setIntProperty(propertyId, areaId, Light.STATE_OFF)
    }

    fun turnOnAllInteriorLights(): Boolean {
        Log.i(TAG, "Turning on all interior lights")
        var success = true
        listOf(Light.AREA_1, Light.AREA_2, Light.AREA_4).forEach { areaId ->
            if (!vehicleHelper.setIntProperty(propertyId, areaId, Light.STATE_ON)) {
                success = false
            }
        }
        return success
    }

    fun turnOffAllInteriorLights(): Boolean {
        Log.i(TAG, "Turning off all interior lights")
        var success = true
        listOf(Light.AREA_1, Light.AREA_2, Light.AREA_4).forEach { areaId ->
            if (!vehicleHelper.setIntProperty(propertyId, areaId, Light.STATE_OFF)) {
                success = false
            }
        }
        return success
    }

    fun isLightOn(areaId: Int = Light.AREA_1): Boolean {
        return vehicleHelper.getIntProperty(propertyId, areaId) == Light.STATE_ON
    }

    fun areAllLightsOn(): Boolean {
        return listOf(Light.AREA_1, Light.AREA_2, Light.AREA_4).all { areaId ->
            vehicleHelper.getIntProperty(propertyId, areaId) == Light.STATE_ON
        }
    }
}
