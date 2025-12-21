package com.ggscc.app.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import com.ggscc.app.car.MediaCenterBridge
import ecarx.xsf.mediacenter.MusicPlaybackInfo
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MediaSessionCoordinator(
    var context: Context,
    private val gateway: MediaCenterBridge,
    private val onPlaybackStarted: (() -> Unit)? = null
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentController: MediaController? = null
    private var callbackHandler: MediaControllerCallbackHandler? = null
    private var progressJob: Job? = null
    private var lastPosition: Long = 0
    private var positionUpdateTime: Long = 0

    private var lastNativeSessionDetectionTime: Long = 0
    private var nativeSessionCooldownPeriod: Long = 5000
    private var lastNativeSessionPackage: String? = null

    private var currentSessionPackage: String? = null
    private var pollingJob: Job? = null

    private val nativePackages = setOf(
        "com.android.bluetooth",
        "com.ecarx.mediacenter",
        "com.flyme"
    )

    fun start() {
        startPolling()
        Log.i(TAG, "Started polling media sessions")
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                pollMediaSessions()
                delay(1000)
            }
        }
    }

    private fun pollMediaSessions() {
        val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val activeSessions = try {
            mediaSessionManager.getActiveSessions(null)
        } catch (e: SecurityException) {
            Log.w(TAG, "No permission to get active sessions: ${e.message}")
            return
        }

        if (activeSessions.isEmpty()) {
            cleanupCurrentController()
            return
        }

        val currentTime = SystemClock.elapsedRealtime()
        val inCooldown = currentTime - lastNativeSessionDetectionTime < nativeSessionCooldownPeriod

        if (inCooldown && lastNativeSessionPackage == "com.android.bluetooth") {
            Log.d(TAG, "In cooldown period for Bluetooth detection")
            handleNonNativeSessions(activeSessions)
            return
        }

        val playingSessions = activeSessions.filter { isActuallyPlaying(it) }
        val nativePlayingSession = playingSessions.firstOrNull { isNativeSession(it) }

        if (nativePlayingSession != null) {
            lastNativeSessionDetectionTime = currentTime
            lastNativeSessionPackage = nativePlayingSession.packageName
            Log.i(TAG, "Native session ${nativePlayingSession.packageName} detected")
            cleanupCurrentController()
            return
        }

        handleNonNativeSessions(activeSessions)
    }

    private fun handleNonNativeSessions(activeSessions: List<MediaController>) {
        val playingSessions = activeSessions.filter { isActuallyPlaying(it) && !isNativeSession(it) }
        val nonNativePlayingSession = playingSessions.firstOrNull()

        if (nonNativePlayingSession != null) {
            if (nonNativePlayingSession.packageName != currentController?.packageName) {
                Log.i(TAG, "Switching to playing session: ${nonNativePlayingSession.packageName}")
                switchToController(nonNativePlayingSession)
            } else {
                updateCurrentSessionState()
            }
            return
        }

        val currentSession = currentController
        if (currentSession != null && isSessionActive(currentSession)) {
            Log.d(TAG, "Current session ${currentSession.packageName} is paused but active")
            progressJob?.cancel()
        } else {
            Log.d(TAG, "No valid sessions found, cleaning up")
            gateway.updateWithPlaybackInfo(MusicPlaybackInfo().apply { playbackStatus = 2 })
            cleanupCurrentController()
        }
    }

    private fun updateCurrentSessionState() {
        val current = currentController ?: return
        val playbackState = current.playbackState ?: return

        if (isPlayingState(playbackState.state)) {
            if (progressJob?.isActive != true) {
                startProgressLoop(current)
            }

            val info = MetadataAdapter.toPlaybackInfo(current, context)
            gateway.updateWithPlaybackInfo(info)
        } else if (isSessionActive(current)) {
            progressJob?.cancel()
        } else {
            Log.i(TAG, "Current session ${current.packageName} is no longer active")
            cleanupCurrentController()
        }
    }

    private fun isNativeSession(controller: MediaController): Boolean {
        return nativePackages.any { native ->
            controller.packageName?.startsWith(native) == true
        }
    }

    private fun isPlayingState(state: Int): Boolean {
        return state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
    }

    private fun isActuallyPlaying(session: MediaController): Boolean {
        val playbackState = session.playbackState ?: return false
        val metadata = session.metadata ?: return false

        if (!isPlayingState(playbackState.state)) {
            return false
        }

        val hasValidMetadata = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)?.isNotEmpty() == true ||
                metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)?.isNotEmpty() == true

        if (!hasValidMetadata) {
            return false
        }

        if (isNativeSession(session)) {
            return isNativeSessionActuallyPlaying(session, playbackState)
        }

        return true
    }

    private fun isNativeSessionActuallyPlaying(session: MediaController, playbackState: PlaybackState): Boolean {
        if (session.packageName != "com.android.bluetooth") {
            val currentTime = SystemClock.elapsedRealtime()
            val lastUpdateTime = playbackState.lastPositionUpdateTime
            val isRecentlyUpdated = currentTime - lastUpdateTime < 1500
            val hasNonZeroPosition = playbackState.position > 0
            return isRecentlyUpdated && hasNonZeroPosition
        }

        val currentPosition = playbackState.position
        val currentTime = SystemClock.elapsedRealtime()
        val lastUpdateTime = playbackState.lastPositionUpdateTime

        if (session.packageName == currentSessionPackage) {
            val positionDiff = abs(currentPosition - lastPosition)

            if (positionDiff < 500) {
                Log.d(TAG, "Bluetooth position change too small ($positionDiff ms)")
                return false
            } else {
                lastPosition = currentPosition
                positionUpdateTime = currentTime
                return true
            }
        } else {
            lastPosition = currentPosition
            positionUpdateTime = currentTime
            currentSessionPackage = session.packageName
        }

        val timeSinceUpdate = currentTime - lastUpdateTime
        if (timeSinceUpdate > 2000) {
            Log.d(TAG, "Bluetooth no recent updates for $timeSinceUpdate ms")
            return false
        }

        return false
    }

    private fun isSessionActive(session: MediaController): Boolean {
        val playbackState = session.playbackState ?: return false
        val metadata = session.metadata ?: return false

        val hasValidMetadata = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)?.isNotEmpty() == true ||
                metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)?.isNotEmpty() == true

        if (!hasValidMetadata) {
            return false
        }

        val isActiveState = playbackState.state != PlaybackState.STATE_STOPPED &&
                playbackState.state != PlaybackState.STATE_ERROR &&
                playbackState.state != PlaybackState.STATE_NONE

        val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val activeSessionPackages = try {
            mediaSessionManager.getActiveSessions(null).map { it.packageName }
        } catch (e: SecurityException) {
            emptyList()
        }

        return isActiveState && session.packageName in activeSessionPackages
    }

    private fun switchToController(controller: MediaController) {
        cleanupCurrentController()

        try {
            val handler = MediaControllerCallbackHandler(
                controller,
                gateway,
                onSessionDestroyed = { },
                onPlaybackStarted = onPlaybackStarted
            )

            controller.registerCallback(handler, Handler(context.mainLooper))
            currentController = controller
            callbackHandler = handler

            val info = MetadataAdapter.toPlaybackInfo(controller, context)
            gateway.registerPlayer(controller.packageName ?: context.packageName)
            gateway.updateWithPlaybackInfo(info)

            val playbackState = controller.playbackState
            if (playbackState != null && isPlayingState(playbackState.state)) {
                onPlaybackStarted?.invoke()
            }

            startProgressLoop(controller)
        } catch (e: Exception) {
            Log.e(TAG, "Error switching to controller", e)
            cleanupCurrentController()
        }
    }

    private fun cleanupCurrentController() {
        callbackHandler?.let { handler ->
            currentController?.unregisterCallback(handler)
        }
        currentController = null
        callbackHandler = null
        progressJob?.cancel()
        gateway.unregisterPlayer()
    }

    private fun startProgressLoop(controller: MediaController) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val playbackState = controller.playbackState
                if (playbackState != null && isPlayingState(playbackState.state)) {
                    gateway.updateProgress(playbackState.position)
                }
                delay(100)
            }
        }
    }

    fun stop() {
        pollingJob?.cancel()
        progressJob?.cancel()
        scope.cancel()
        cleanupCurrentController()
        Log.d(TAG, "Stopped polling")
    }

    fun cleanExpiredCache() {
        val ttl = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        listOfNotNull(
            context.externalCacheDir,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        ).forEach { base ->
            File(base, "art").listFiles()
                ?.filter { it.isFile && it.lastModified() < ttl }
                ?.forEach { it.delete() }
        }
    }

    companion object {
        private const val TAG = "MediaCoordinator"
    }
}
