package com.ggscc.app.controllers

import android.os.Handler
import android.os.Looper
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.VehicleInfo as Props

data class VehicleInfo(
    val odometer: Float?,
    val batteryPercent: Float?,
    val batteryTemp: Float?,
    val batteryVoltage: Float?,
    val fuelPercent: Int?,
    val rangeEv: Float?,
    val rangeFuel: Float?,
    val maintenanceMileage: Float?,
    val maintenanceTime: Float?,
    val mileageSinceLastAll: Float?,
    val mileageSinceLastOil: Float?,
    val mileageSinceLastEv: Float?,
    val chargingVoltage: Float?,
    val chargingCurrent: Float?,
    val chargingPower: Float?,
    val chargingTimeMinutes: Float?,
    val chargingPlugState: Int?,
    val chargingSpeed: Float?,
    val chargingEnergy: Float?,
    val chargingDcPower: Float?,
    val dischargingPower: Float?,
    val chargingState: Int?,
    val energyDriving: Float?,
    val energyClimate: Float?,
    val energyBattery: Float?,
    val energyOther: Float?
)

class VehicleInfoController(private val vehicleHelper: VehiclePropertyHelper) {

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    var onInfoUpdated: ((VehicleInfo) -> Unit)? = null

    fun startAutoUpdate(intervalMs: Long = 10000) {
        stopAutoUpdate()
        updateRunnable = object : Runnable {
            override fun run() {
                onInfoUpdated?.invoke(getInfo())
                handler.postDelayed(this, intervalMs)
            }
        }
        handler.post(updateRunnable!!)
    }

    fun stopAutoUpdate() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    fun getInfo(): VehicleInfo {
        val pm = vehicleHelper.propertyManager
        return VehicleInfo(
            odometer = pm?.safeFloat(Props.ODOMETER),
            batteryPercent = pm?.safeFloat(Props.EV_BATTERY_PERCENTAGE),
            batteryTemp = pm?.safeFloat(Props.EV_BATTERY_TEMP),
            batteryVoltage = pm?.safeFloat(Props.EV_BATTERY_VOLTAGE),
            fuelPercent = pm?.safeInt(Props.FUEL_PERCENTAGE, 1),
            rangeEv = pm?.safeFloat(Props.RANGE_EV),
            rangeFuel = pm?.safeFloat(Props.RANGE_FUEL),
            maintenanceMileage = pm?.safeFloat(Props.MAINTENANCE_MILEAGE),
            maintenanceTime = pm?.safeFloat(Props.MAINTENANCE_TIME),
            mileageSinceLastAll = pm?.safeFloat(Props.MILEAGE_SINCE_LAST_ALL),
            mileageSinceLastOil = pm?.safeFloat(Props.MILEAGE_SINCE_LAST_OIL),
            mileageSinceLastEv = pm?.safeFloat(Props.MILEAGE_SINCE_LAST_EV),
            chargingVoltage = pm?.safeFloat(Props.CHARGING_VOLTAGE)?.takeIf { it > 0 },
            chargingCurrent = pm?.safeFloat(Props.CHARGING_CURRENT)?.takeIf { it > 0 },
            chargingPower = pm?.safeFloat(Props.CHARGING_POWER)?.takeIf { it > 0 },
            chargingTimeMinutes = pm?.safeFloat(Props.CHARGING_TIME)?.takeIf { it > 0 },
            chargingPlugState = pm?.safeInt(Props.CHARGING_PLUG_STATE),
            chargingSpeed = pm?.safeFloat(Props.CHARGING_SPEED)?.takeIf { it > 0 },
            chargingEnergy = pm?.safeFloat(Props.CHARGING_ENERGY)?.takeIf { it > 0 },
            chargingDcPower = pm?.safeFloat(Props.CHARGING_DC_POWER)?.takeIf { it > 0 },
            dischargingPower = pm?.safeFloat(Props.DISCHARGING_POWER)?.takeIf { it > 0 },
            chargingState = pm?.safeInt(Props.CHARGING_STATE),
            energyDriving = pm?.safeFloat(Props.ENERGY_DRIVING),
            energyClimate = pm?.safeFloat(Props.ENERGY_CLIMATE),
            energyBattery = pm?.safeFloat(Props.ENERGY_BATTERY),
            energyOther = pm?.safeFloat(Props.ENERGY_OTHER)
        )
    }

    private fun android.car.hardware.property.CarPropertyManager.safeFloat(propId: Int, areaId: Int = 0): Float? {
        return try { getFloatProperty(propId, areaId).takeIf { !it.isNaN() && it > -100 } } catch (e: Exception) { null }
    }

    private fun android.car.hardware.property.CarPropertyManager.safeInt(propId: Int, areaId: Int = 0): Int? {
        return try { getIntProperty(propId, areaId).takeIf { it >= 0 } } catch (e: Exception) { null }
    }
}
