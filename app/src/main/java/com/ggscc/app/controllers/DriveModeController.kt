package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.DriveMode

class DriveModeController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "DriveModeController"
    }

    fun getDriveMode(): Int {
        val mode = vehicleHelper.getIntProperty(DriveMode.PROPERTY_ID, DriveMode.AREA)
        Log.d(TAG, "Current drive mode: $mode")
        return mode
    }

    fun setDriveMode(mode: Int): Boolean {
        val modeName = when (mode) {
            DriveMode.RANGE_EXT -> "RANGE_EXT"
            DriveMode.SPORT -> "SPORT"
            DriveMode.ELECTRIC -> "ELECTRIC"
            DriveMode.INTELLIGENT -> "INTELLIGENT"
            else -> "UNKNOWN"
        }
        Log.i(TAG, "Setting drive mode to $modeName ($mode)")
        return vehicleHelper.setIntProperty(DriveMode.PROPERTY_ID, DriveMode.AREA, mode)
    }
}
