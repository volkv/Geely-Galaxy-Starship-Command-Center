package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Mirror

class MirrorController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "MirrorController"
    }

    fun foldMirrors(): Boolean {
        Log.i(TAG, "Folding mirrors")
        return vehicleHelper.setBoolProperty(Mirror.PROPERTY_ID, Mirror.AREA, true)
    }

    fun unfoldMirrors(): Boolean {
        Log.i(TAG, "Unfolding mirrors")
        return vehicleHelper.setBoolProperty(Mirror.PROPERTY_ID, Mirror.AREA, false)
    }

    fun getMirrorState(): Boolean {
        return vehicleHelper.getBoolProperty(Mirror.PROPERTY_ID, Mirror.AREA)
    }
}
