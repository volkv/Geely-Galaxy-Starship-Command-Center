package com.ggscc.app.car

import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import com.ggscc.app.tools.PropertyConstants

class WheelButtonHandler(
    private val context: Context,
    private val vehiclePropertyHelper: VehiclePropertyHelper
) {
    companion object {
        private const val TAG = "WheelButtonHandler"
    }

    private val WHEEL_KEYS = mapOf(
        PropertyConstants.WheelButton.KEY_LEFT to "LEFT",
        PropertyConstants.WheelButton.KEY_RIGHT to "RIGHT",
        PropertyConstants.WheelButton.KEY_CONFIRM to "CONFIRM"
    )

    private val KEY_PRIO_FOR_DIM = 557872748

    // Track button states to prevent duplicate events
    private val buttonStates = mutableMapOf<String, Boolean>().apply {
        WHEEL_KEYS.values.forEach { put(it, false) }
    }

    private var lastConfirmState = 0
    
    // Debounce tracking
    private var lastSendTime = 0L
    private var lastKeyCode = -1

    private val callback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            WHEEL_KEYS[value.propertyId]?.let { buttonName ->
                val state = value.value as Int
                val isPressed = state == 1

                if (buttonStates[buttonName] != isPressed) {
                    buttonStates[buttonName] = isPressed

                    when (buttonName) {
                        "LEFT" -> {
                            if (isPressed) Log.i(TAG, "LEFT pressed - ignoring (handled by MusicClient)")
                        }
                        "RIGHT" -> {
                            if (isPressed) Log.i(TAG, "RIGHT pressed - ignoring (handled by MusicClient)")
                        }
                        "CONFIRM" -> {
                            if (isPressed) Log.i(TAG, "CONFIRM pressed - ignoring (handled by MusicClient)")
                        }
                    }
                }
            }
        }

        override fun onErrorEvent(propId: Int, zone: Int) {
            Log.e(TAG, "Error event for property: $propId, zone: $zone")
        }
    }

    private val dimCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            val intValue = value.value as? Int ?: return
            val confirmState = intValue and 0x1
            val bit1 = (intValue shr 1) and 0x1
            val bit2 = (intValue shr 2) and 0x1
            val bit3 = (intValue shr 3) and 0x1
            val bit4 = (intValue shr 4) and 0x1
            val bit5 = (intValue shr 5) and 0x1
            val bit6 = (intValue shr 6) and 0x1
            val bit7 = (intValue shr 7) and 0x1

            if (confirmState != lastConfirmState) {
                lastConfirmState = confirmState
                if (confirmState == 1) {
                    Log.i(TAG, "CONFIRM pressed via DIM - ignoring in WheelButtonHandler")
                    // sendMediaKeyPair(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                }
            }
        }

        override fun onErrorEvent(propId: Int, zone: Int) {
            Log.e(TAG, "DIM Error event for property: $propId, zone: $zone")
        }
    }

    fun startListening() {
        Log.i(TAG, "Starting wheel button listener")
        registerCallbacks()
    }

    private fun registerCallbacks() {
        val pm = vehiclePropertyHelper.propertyManager
        if (pm == null) {
            Log.e(TAG, "CarPropertyManager is null, cannot register callbacks")
            return
        }

        WHEEL_KEYS.keys.forEach { propId ->
            try {
                pm.registerCallback(
                    callback,
                    propId,
                    CarPropertyManager.SENSOR_RATE_ONCHANGE
                )
                Log.i(TAG, "Registered callback for $propId (${WHEEL_KEYS[propId]})")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register callback for $propId", e)
            }
        }

        try {
            pm.registerCallback(
                dimCallback,
                KEY_PRIO_FOR_DIM,
                CarPropertyManager.SENSOR_RATE_ONCHANGE
            )
            Log.i(TAG, "Registered DIM callback for $KEY_PRIO_FOR_DIM")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register DIM callback", e)
        }
    }

    private fun sendMediaKeyPair(keyCode: Int) {
        val now = SystemClock.uptimeMillis()
        val debounceTime = if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) 500L else 200L

        if (keyCode == lastKeyCode && now - lastSendTime < debounceTime) {
            return
        }

        lastSendTime = now
        lastKeyCode = keyCode

        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        val downTime = SystemClock.uptimeMillis()

        val downEvent = KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        val upEvent = KeyEvent(downTime, downTime + 10, KeyEvent.ACTION_UP, keyCode, 0)

        try {
            Log.i(TAG, "Dispatching media key pair: keyCode=$keyCode")
            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending media key pair for keyCode=$keyCode: $e")
        }
    }

    fun stopListening() {
        try {
            val pm = vehiclePropertyHelper.propertyManager ?: return
            WHEEL_KEYS.keys.forEach { propId ->
                pm.unregisterCallback(callback, propId)
            }
            pm.unregisterCallback(dimCallback, KEY_PRIO_FOR_DIM)
            Log.i(TAG, "Stopped listening to wheel buttons")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering callbacks", e)
        }
    }
}
