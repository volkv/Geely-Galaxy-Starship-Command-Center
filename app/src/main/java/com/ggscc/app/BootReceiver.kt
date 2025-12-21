package com.ggscc.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ggscc.app.util.DeviceIdleWhitelistHelper

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed, starting MediaBridgeService")
            DeviceIdleWhitelistHelper.ensureDefaultPackagesWhitelisted()
            MediaBridgeService.start(context)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
