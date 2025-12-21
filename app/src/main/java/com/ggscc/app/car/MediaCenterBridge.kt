package com.ggscc.app.car

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.ggscc.app.media.MediaSessionCoordinator
import ecarx.xsf.mediacenter.IMediaCenterClientToken
import ecarx.xsf.mediacenter.IMediaCenterSvc
import ecarx.xsf.mediacenter.MusicClient
import ecarx.xsf.mediacenter.MusicPlaybackInfo

class MediaCenterBridge(val context: Context) {

    private var svc: IMediaCenterSvc? = null
    private var token: IMediaCenterClientToken? = null
    private var mediaSessionCoordinator: MediaSessionCoordinator? = null
    private var currentMusicClient: MusicClient? = null

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.w(TAG, "=== onServiceConnected called, binder=$binder ===")
            svc = IMediaCenterSvc.Stub.asInterface(binder)
            isBound = true
            Log.w(TAG, "MediaCenterService connected, svc=$svc")
            mediaSessionCoordinator?.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            svc = null
            isBound = false
            Log.w(TAG, "MediaCenterService disconnected")
        }
    }

    fun setMediaSessionCoordinator(coordinator: MediaSessionCoordinator) {
        mediaSessionCoordinator = coordinator
    }

    fun isConnected(): Boolean {
        return isBound && svc != null && isServiceAlive()
    }

    private fun isServiceAlive(): Boolean {
        return try {
            svc?.asBinder()?.pingBinder() == true
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting service alive status: $e")
            false
        }
    }

    fun isConnectionInProgress(): Boolean {
        return isConnecting &&
                System.currentTimeMillis() - lastConnectionTime < CONNECTION_TIMEOUT_MS
    }

    fun start() {
        Log.w(TAG, "=== start() called, isConnected=${isConnected()}, isConnecting=$isConnecting ===")
        if (isConnected() || isConnectionInProgress()) {
            Log.w(TAG, "Service already connected or connecting")
            return
        }

        Log.w(TAG, "Binding to MediaCenterService...")
        isConnecting = true
        lastConnectionTime = System.currentTimeMillis()

        val intent = Intent(SERVICE_ACTION).apply {
            setComponent(ComponentName(SERVICE_PKG, SERVICE_CLS))
        }

        try {
            val bound = context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
            if (!bound) {
                Log.e(TAG, "=== bindService returned FALSE ===")
                isConnecting = false
            } else {
                Log.w(TAG, "=== bindService returned TRUE, waiting... ===")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Binding failed - SecurityException", e)
            isConnecting = false
        } catch (e: Exception) {
            Log.e(TAG, "Binding failed - Exception", e)
            isConnecting = false
        }
    }

    fun stop() {
        mediaSessionCoordinator?.stop()

        if (isBound) {
            try {
                updateWithPlaybackInfo(MusicPlaybackInfo().apply { playbackStatus = 2 })
                context.unbindService(conn)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Service already unbound", e)
            }
            isBound = false
        }
        isConnecting = false
    }

    fun registerPlayer(pkg: String) {
        Log.w(TAG, "=== registerPlayer called for: $pkg, svc=$svc ===")
        val svc = svc ?: run {
            Log.e(TAG, "registerPlayer: svc is NULL!")
            return
        }
        try {
            val musicClient = MusicClient(context)
            currentMusicClient = musicClient
            Log.w(TAG, "Calling registerInMusic for: $pkg")
            token = svc.registerInMusic(pkg, musicClient)
            if (token != null) {
                Log.w(TAG, "=== REGISTERED successfully: $pkg, token: $token ===")
                requestPlay()
            } else {
                Log.e(TAG, "=== Registration REJECTED for $pkg (whitelist/signature) ===")
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "IPC failed during registerPlayer", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in registerPlayer", e)
        }
    }

    fun getCurrentFocusClient(): String? {
        return token?.let { svc?.queryCurrentFocusClient(it) }
    }

    fun unregisterPlayer() {
        if (token == null) {
            return
        }
        try {
            svc?.unregister(token!!)
        } catch (e: RemoteException) {
            Log.w(TAG, "Failed to unregister", e)
        }
        token = null
        currentMusicClient = null
    }

    fun requestPlay(): Boolean {
        return try {
            val currentToken = token ?: return false
            svc?.let { service ->
                val result = service.requestPlay(currentToken)
                Log.d(TAG, "requestPlay result: $result")
                result
            } ?: false
        } catch (e: RemoteException) {
            Log.w(TAG, "IPC failed in requestPlay", e)
            false
        }
    }

    fun updateWithPlaybackInfo(playbackInfo: MusicPlaybackInfo) {
        currentMusicClient?.updatePlaybackInfo(playbackInfo)

        token?.let { token ->
            if (playbackInfo.playbackStatus == 1) {
                requestPlay()
            }
            try {
                svc?.updateMusicPlaybackState(token, playbackInfo)
                svc?.updateCurrentSourceType(token, playbackInfo.sourceType)
                Log.d(TAG, "Updated playback info: ${playbackInfo.title}, status: ${playbackInfo.playbackStatus}")
            } catch (e: RemoteException) {
                Log.w(TAG, "Failed to update playback info", e)
            }
        }
    }

    fun updateProgress(position: Long) {
        currentMusicClient?.updateProgress(position)

        token?.let { token ->
            try {
                svc?.updateCurrentProgress(token, position)
            } catch (e: RemoteException) {
                Log.w(TAG, "Failed to update progress", e)
            }
        }
    }

    companion object {
        private const val TAG = "MediaCenterBridge"
        private const val SERVICE_ACTION = "ecarx.xsf.MEDIA_CENTER_SERVICE"
        private const val SERVICE_PKG = "ecarx.xsf.mediacenter"
        private const val SERVICE_CLS = "ecarx.xsf.mediacenter.MediaCenterService"
        private var isBound = false
        private var isConnecting = false
        private var lastConnectionTime: Long = 0
        private const val CONNECTION_TIMEOUT_MS = 3000L
    }
}
