package com.ggscc.app

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.ggscc.app.car.MediaCenterBridge
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.car.WheelButtonHandler
import com.ggscc.app.controllers.AmbientLightController
import com.ggscc.app.media.MediaSessionCoordinator
import com.ggscc.app.util.ForegroundServiceHelper
import com.ggscc.app.util.DeviceIdleWhitelistHelper

class MediaBridgeService : Service() {

    private lateinit var mediaCenterBridge: MediaCenterBridge
    private lateinit var mediaSessionCoordinator: MediaSessionCoordinator
    private lateinit var prefs: SharedPreferences
    private var vehiclePropertyHelper: VehiclePropertyHelper? = null
    private var wheelButtonHandler: WheelButtonHandler? = null
    private var ambientLightController: AmbientLightController? = null

    private val voiceButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_VOICE_ASSIST) {
                toggleMainActivity()
            }
        }
    }

    private fun toggleMainActivity() {
        if (MainActivity.isInForeground) {
            Log.i(TAG, "Voice button pressed, minimizing MainActivity")
            sendBroadcast(Intent(ACTION_MINIMIZE_ACTIVITY).setPackage(packageName))
        } else {
            Log.i(TAG, "Voice button pressed, launching MainActivity")
            val launchIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(launchIntent)
        }
    }

    private fun registerVoiceButtonReceiver() {
        val filter = IntentFilter(ACTION_VOICE_ASSIST).apply {
            priority = 999
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            registerReceiver(voiceButtonReceiver, filter, 2)
        } else {
            registerReceiver(voiceButtonReceiver, filter)
        }
        Log.i(TAG, "Voice button receiver registered")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service creating")

        prefs = getSharedPreferences("car_control_prefs", Context.MODE_PRIVATE)

        ForegroundServiceHelper.createChannel(this, CHANNEL_ID, "Media Bridge Service", "Enables steering wheel media controls")
        startForeground(NOTIFICATION_ID, ForegroundServiceHelper.createNotification(
            this, CHANNEL_ID, "Media Bridge", "Steering wheel controls active", android.R.drawable.ic_media_play
        ))

        DeviceIdleWhitelistHelper.ensureDefaultPackagesWhitelisted()
        registerVoiceButtonReceiver()

        try {
            vehiclePropertyHelper = VehiclePropertyHelper(applicationContext)
            vehiclePropertyHelper?.takeIf { it.isConnected }?.let { helper ->
                try {
                    wheelButtonHandler = WheelButtonHandler(applicationContext, helper)
                    wheelButtonHandler?.startListening()
                    Log.i(TAG, "WheelButtonHandler started")
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting WheelButtonHandler", e)
                }
                try {
                    ambientLightController = AmbientLightController(helper)
                    Log.i(TAG, "AmbientLightController initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing AmbientLightController", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing VehiclePropertyHelper", e)
        }

        try {
            mediaCenterBridge = MediaCenterBridge(applicationContext)
            mediaSessionCoordinator = MediaSessionCoordinator(this, mediaCenterBridge) {
                onThirdPartyPlaybackStarted()
            }
            mediaSessionCoordinator.cleanExpiredCache()
            mediaCenterBridge.setMediaSessionCoordinator(mediaSessionCoordinator)
            mediaCenterBridge.start()
            Log.i(TAG, "Service created and MediaCenterBridge started")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaCenterBridge", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG, "Service destroying")
        try { unregisterReceiver(voiceButtonReceiver) } catch (e: Exception) { Log.w(TAG, "Error unregistering receiver", e) }
        mediaCenterBridge.stop()
        wheelButtonHandler?.stopListening()
        vehiclePropertyHelper?.disconnect()
        super.onDestroy()
    }

    private fun onThirdPartyPlaybackStarted() {
        val colorMusicEnabled = prefs.getBoolean(PrefsConstants.COLOR_MUSIC_ENABLED, false)
        if (!colorMusicEnabled) {
            Log.d(TAG, "Third-party playback started, but color music is disabled")
            return
        }

        Log.i(TAG, "Third-party playback started, activating color music")
        ambientLightController?.let { controller ->
            val result = controller.activateColorMusic()
            Log.i(TAG, "Color music activation result: $result")
        }
    }

    companion object {
        private const val TAG = "MediaBridgeService"
        private const val CHANNEL_ID = "media_bridge_channel"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_VOICE_ASSIST = "ecarx.intent.action.ECARX_KEY_RVOICEASSIST_EVENT"
        const val ACTION_MINIMIZE_ACTIVITY = "com.ggscc.app.ACTION_MINIMIZE"

        fun start(context: Context) {
            val intent = Intent(context, MediaBridgeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MediaBridgeService::class.java))
        }
    }
}
