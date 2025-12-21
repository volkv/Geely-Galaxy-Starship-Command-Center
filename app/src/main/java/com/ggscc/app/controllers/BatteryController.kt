package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants

class BatteryController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "BatteryController"
    }

    fun isBatteryModeEnabled(): Boolean {
        return try {
            val value = vehicleHelper.getIntProperty(
                PropertyConstants.Battery.MODE_PROPERTY_ID,
                PropertyConstants.Battery.AREA
            )
            Log.i(TAG, "Get battery mode: value=$value, MODE_CHARGE=${PropertyConstants.Battery.MODE_CHARGE}")
            value == PropertyConstants.Battery.MODE_CHARGE
        } catch (e: Exception) {
            Log.e(TAG, "Error getting battery mode", e)
            false
        }
    }

    fun setBatteryMode(enabled: Boolean) {
        try {
            val value = if (enabled) {
                PropertyConstants.Battery.MODE_CHARGE
            } else {
                PropertyConstants.Battery.MODE_NORMAL
            }
            Log.i(TAG, "Setting battery mode: enabled=$enabled, value=$value, propertyId=${PropertyConstants.Battery.MODE_PROPERTY_ID}")
            vehicleHelper.setIntProperty(
                PropertyConstants.Battery.MODE_PROPERTY_ID,
                PropertyConstants.Battery.AREA,
                value
            )
            Log.i(TAG, "Battery mode set to: ${if (enabled) "CHARGE" else "NORMAL"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting battery mode", e)
        }
    }

    fun isParkingChargeEnabled(): Boolean {
        return try {
            vehicleHelper.getBoolProperty(
                PropertyConstants.Battery.PARKING_CHARGE_PROPERTY_ID,
                PropertyConstants.Battery.AREA
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting parking charge", e)
            false
        }
    }

    fun setParkingCharge(enabled: Boolean) {
        try {
            Log.i(TAG, "Setting parking charge: enabled=$enabled, propertyId=${PropertyConstants.Battery.PARKING_CHARGE_PROPERTY_ID}")
            vehicleHelper.setBoolProperty(
                PropertyConstants.Battery.PARKING_CHARGE_PROPERTY_ID,
                PropertyConstants.Battery.AREA,
                enabled
            )
            Log.i(TAG, "Parking charge set to: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting parking charge", e)
        }
    }
}
