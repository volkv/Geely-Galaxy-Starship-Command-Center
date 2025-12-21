package com.ggscc.app.util

import android.os.IBinder
import android.util.Log

object DeviceIdleWhitelistHelper {
    private const val TAG = "DeviceIdleWhitelist"

    private val defaultPackages = listOf(
        "com.spotify.music"
    )

    fun ensureDefaultPackagesWhitelisted() {
        ensurePackagesWhitelisted(defaultPackages)
    }

    fun ensurePackagesWhitelisted(packages: List<String>): Boolean {
        var allSucceeded = true
        for (pkg in packages) {
            val result = whitelistPackage(pkg)
            allSucceeded = allSucceeded && result
        }
        return allSucceeded
    }

    private fun whitelistPackage(packageName: String): Boolean {
        return try {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val binder = getServiceMethod.invoke(null, "deviceidle") as? IBinder
            if (binder == null) {
                Log.w(TAG, "deviceidle service not available")
                return false
            }

            val stubClass = Class.forName("android.os.IDeviceIdleController\$Stub")
            val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
            val controller = asInterfaceMethod.invoke(null, binder)

            val addMethod = controller.javaClass.getMethod("addPowerSaveWhitelistApp", String::class.java)
            addMethod.invoke(controller, packageName)
            Log.i(TAG, "Whitelisted $packageName from device idle")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing permission to whitelist $packageName", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to whitelist $packageName", e)
            false
        }
    }
}
