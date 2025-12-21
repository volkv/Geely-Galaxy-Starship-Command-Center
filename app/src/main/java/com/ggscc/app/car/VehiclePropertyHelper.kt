package com.ggscc.app.car

import android.car.Car
import android.car.hardware.CarPropertyConfig
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log

class VehiclePropertyHelper(context: Context) {
    companion object {
        private const val TAG = "VehiclePropertyHelper"
    }

    private var car: Car? = null
    var propertyManager: CarPropertyManager? = null
        private set

    var isConnected: Boolean = false
        private set

    init {
        try {
            car = Car.createCar(context)
            propertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            isConnected = propertyManager != null
            Log.i(TAG, "Car API connected: $isConnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Car API", e)
            isConnected = false
        }
    }

    fun getIntProperty(propertyId: Int, areaId: Int): Int {
        return try {
            propertyManager?.let { pm ->
                if (pm.isPropertyAvailable(propertyId, areaId)) {
                    pm.getIntProperty(propertyId, areaId)
                } else {
                    Log.w(TAG, "Property $propertyId not available for area $areaId")
                    -1
                }
            } ?: -1
        } catch (e: Exception) {
            Log.e(TAG, "Error getting int property $propertyId", e)
            -1
        }
    }

    fun getBoolProperty(propertyId: Int, areaId: Int): Boolean {
        return try {
            propertyManager?.let { pm ->
                if (pm.isPropertyAvailable(propertyId, areaId)) {
                    pm.getBooleanProperty(propertyId, areaId)
                } else {
                    Log.w(TAG, "Property $propertyId not available for area $areaId")
                    false
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bool property $propertyId", e)
            false
        }
    }

    fun getFloatProperty(propertyId: Int, areaId: Int): Float {
        return try {
            propertyManager?.let { pm ->
                if (pm.isPropertyAvailable(propertyId, areaId)) {
                    pm.getFloatProperty(propertyId, areaId)
                } else {
                    Log.w(TAG, "Property $propertyId not available for area $areaId")
                    -1f
                }
            } ?: -1f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting float property $propertyId", e)
            -1f
        }
    }

    fun setIntProperty(propertyId: Int, areaId: Int, value: Int): Boolean {
        Log.i(TAG, "setIntProperty: id=$propertyId, area=$areaId, value=$value")
        return try {
            propertyManager?.let { pm ->
                if (pm.isPropertyAvailable(propertyId, areaId)) {
                    pm.setIntProperty(propertyId, areaId, value)
                    true
                } else {
                    Log.e(TAG, "Property $propertyId not available for area $areaId")
                    false
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting int property $propertyId", e)
            false
        }
    }

    fun setBoolProperty(propertyId: Int, areaId: Int, value: Boolean): Boolean {
        Log.i(TAG, "setBoolProperty: id=$propertyId, area=$areaId, value=$value")
        return try {
            propertyManager?.let { pm ->
                if (pm.isPropertyAvailable(propertyId, areaId)) {
                    pm.setBooleanProperty(propertyId, areaId, value)
                    true
                } else {
                    Log.e(TAG, "Property $propertyId not available for area $areaId")
                    false
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting bool property $propertyId", e)
            false
        }
    }

    fun setFloatProperty(propertyId: Int, areaId: Int, value: Float): Boolean {
        Log.i(TAG, "setFloatProperty: id=$propertyId, area=$areaId, value=$value")
        return try {
            propertyManager?.let { pm ->
                if (pm.isPropertyAvailable(propertyId, areaId)) {
                    pm.setFloatProperty(propertyId, areaId, value)
                    true
                } else {
                    Log.e(TAG, "Property $propertyId not available for area $areaId")
                    false
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting float property $propertyId", e)
            false
        }
    }

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            Log.i(TAG, "PROP CHANGED: id=${value.propertyId}, area=${value.areaId}, value=${value.value}")
        }

        override fun onErrorEvent(propId: Int, zone: Int) {
            Log.e(TAG, "PROP ERROR: id=$propId, zone=$zone")
        }
    }

    fun startLoggingMassageProperties(switchId: Int, powerId: Int, typeId: Int) {
        Log.i(TAG, "Starting massage property logging for IDs: switch=$switchId, power=$powerId, type=$typeId")
        try {
            propertyManager?.registerCallback(propertyCallback, switchId, CarPropertyManager.SENSOR_RATE_ONCHANGE)
            propertyManager?.registerCallback(propertyCallback, powerId, CarPropertyManager.SENSOR_RATE_ONCHANGE)
            propertyManager?.registerCallback(propertyCallback, typeId, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register massage callbacks", e)
        }
    }

    fun getPropertyList(): List<CarPropertyConfig<*>> {
        return try {
            propertyManager?.propertyList?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting property list", e)
            emptyList()
        }
    }

    fun getPropertyCount(): Int = getPropertyList().size

    fun dumpAllProperties() {
        val props = getPropertyList()
        Log.i(TAG, "=== PROPERTY DUMP START === Total: ${props.size}")
        for (prop in props) {
            try {
                val propId = prop.propertyId
                val typeName = prop.propertyType?.simpleName ?: "?"
                val areaIds = prop.areaIds

                val configName = try {
                    val field = prop.javaClass.getDeclaredField("mConfigString")
                    field.isAccessible = true
                    (field.get(prop) as? String)?.takeIf { it.isNotEmpty() }
                } catch (e: Exception) { null }

                val value: String = try {
                    when (typeName) {
                        "Float" -> propertyManager?.getFloatProperty(propId, 0)?.toString() ?: "null"
                        "Integer" -> propertyManager?.getIntProperty(propId, 0)?.toString() ?: "null"
                        "Boolean" -> propertyManager?.getBooleanProperty(propId, 0)?.toString() ?: "null"
                        else -> "[$typeName]"
                    }
                } catch (e: Exception) { "err" }

                val name = configName ?: ""
                Log.i(TAG, "PROP|$propId|$typeName|$value|$name|areas=${areaIds.toList()}")
            } catch (e: Exception) { continue }
        }
        Log.i(TAG, "=== PROPERTY DUMP END ===")
    }

    fun disconnect() {
        try {
            car?.disconnect()
            propertyManager = null
            isConnected = false
            Log.i(TAG, "Disconnected from Car API")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from Car API", e)
        }
    }
}
