package com.ggscc.app.controllers

import android.util.Log
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.Seat
import com.ggscc.app.tools.PropertyConstants.SeatMassage

class SeatMassageController(private val vehicleHelper: VehiclePropertyHelper) {
    companion object {
        private const val TAG = "SeatMassageController"
    }

    private val switchPropertyId = SeatMassage.SWITCH_PROPERTY_ID
    private val powerPropertyId = SeatMassage.POWER_PROPERTY_ID
    private val typePropertyId = SeatMassage.TYPE_PROPERTY_ID

    init {
        vehicleHelper.startLoggingMassageProperties(switchPropertyId, powerPropertyId, typePropertyId)
    }

    fun enableMassage(areaId: Int): Boolean {
        val seatName = if (areaId == Seat.AREA_DRIVER) "driver" else "passenger"
        Log.i(TAG, "Enabling massage for $seatName seat")
        val switchOn = vehicleHelper.setBoolProperty(switchPropertyId, areaId, true)
        val powerSet = vehicleHelper.setIntProperty(powerPropertyId, areaId, SeatMassage.POWER_HIGH)
        return switchOn && powerSet
    }

    fun disableMassage(areaId: Int): Boolean {
        val seatName = if (areaId == Seat.AREA_DRIVER) "driver" else "passenger"
        Log.i(TAG, "Disabling massage for $seatName seat")
        val switchOff = vehicleHelper.setBoolProperty(switchPropertyId, areaId, false)
        val powerOff = vehicleHelper.setIntProperty(powerPropertyId, areaId, SeatMassage.POWER_OFF)
        return switchOff && powerOff
    }

    fun enableDriverMassage(): Boolean = enableMassage(Seat.AREA_DRIVER)
    fun disableDriverMassage(): Boolean = disableMassage(Seat.AREA_DRIVER)
    fun enablePassengerMassage(): Boolean = enableMassage(Seat.AREA_PASSENGER)
    fun disablePassengerMassage(): Boolean = disableMassage(Seat.AREA_PASSENGER)

    fun setMassageType(areaId: Int, type: Int): Boolean {
        val seatName = if (areaId == Seat.AREA_DRIVER) "driver" else "passenger"
        val actualType = (type + 1).coerceIn(1, SeatMassage.MAX_TYPE)
        Log.i(TAG, "Setting massage type for $seatName seat to $actualType (UI index $type)")
        return vehicleHelper.setIntProperty(typePropertyId, areaId, actualType)
    }

    fun isMassageEnabled(areaId: Int): Boolean = vehicleHelper.getBoolProperty(switchPropertyId, areaId)
    fun isDriverMassageEnabled(): Boolean = isMassageEnabled(Seat.AREA_DRIVER)
    fun isPassengerMassageEnabled(): Boolean = isMassageEnabled(Seat.AREA_PASSENGER)

    fun getMassageType(areaId: Int): Int {
        val type = vehicleHelper.getIntProperty(typePropertyId, areaId)
        return (type - 1).coerceAtLeast(0)
    }

    fun getDriverMassageType(): Int = getMassageType(Seat.AREA_DRIVER)
    fun getPassengerMassageType(): Int = getMassageType(Seat.AREA_PASSENGER)
}
