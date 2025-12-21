package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Seat

abstract class SeatLevelController(
    protected val vehicleHelper: VehiclePropertyHelper,
    private val propertyId: Int
) {
    protected abstract val TAG: String

    fun setLevel(areaId: Int, level: Int): Boolean {
        val seatName = if (areaId == Seat.AREA_DRIVER) "driver" else "passenger"
        val clampedLevel = level.coerceIn(Seat.LEVEL_OFF, Seat.LEVEL_HIGH)
        Log.i(TAG, "Setting $seatName seat to level $clampedLevel")
        return vehicleHelper.setIntProperty(propertyId, areaId, clampedLevel)
    }

    fun getLevel(areaId: Int): Int = vehicleHelper.getIntProperty(propertyId, areaId)
}
