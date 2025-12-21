package com.ggscc.app.controllers

import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Window

class WindowController(private val vehicleHelper: VehiclePropertyHelper) {

    private val propertyId = Window.PROPERTY_ID

    fun setWindowPosition(areaId: Int, position: Int): Boolean {
        val clampedPosition = position.coerceIn(Window.POSITION_MIN, Window.POSITION_MAX)
        return vehicleHelper.setIntProperty(propertyId, areaId, clampedPosition)
    }

    fun getWindowPosition(areaId: Int): Int {
        return vehicleHelper.getIntProperty(propertyId, areaId)
    }
}
