package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Trunk

class TrunkController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "TrunkController"
    }

    private val propertyId = Trunk.PROPERTY_ID
    private val areaId = Trunk.AREA_ID

    fun openTrunk(): Boolean {
        Log.i(TAG, "Opening trunk")
        return vehicleHelper.setIntProperty(propertyId, areaId, Trunk.STATE_OPEN)
    }

    fun closeTrunk(): Boolean {
        Log.i(TAG, "Closing trunk")
        return vehicleHelper.setIntProperty(propertyId, areaId, Trunk.STATE_CLOSED)
    }

    fun getTrunkState(): Int {
        return vehicleHelper.getIntProperty(propertyId, areaId)
    }
}
