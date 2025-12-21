package ecarx.xsf.mediacenter

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Context.MEDIA_SESSION_SERVICE
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.util.Log
import android.view.KeyEvent

class MusicClient(private val context: Context) : IMusicClient {

    private val handler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null
    private var lastEventTime = 0L
    private var currentKeyCode = 0

    private val info = MusicPlaybackInfo()

    fun updatePlaybackInfo(newInfo: MusicPlaybackInfo) {
        Log.d(TAG, "Updating playback info: title=${newInfo.title}, artwork=${newInfo.artwork}")
        info.packageName = newInfo.packageName
        info.appName = newInfo.appName
        info.title = newInfo.title
        info.artist = newInfo.artist
        info.album = newInfo.album
        info.lyric = newInfo.lyric
        info.lyricContent = newInfo.lyricContent
        info.lyricSentence = newInfo.lyricSentence
        info.artwork = newInfo.artwork
        info.duration = newInfo.duration
        info.radioMode = newInfo.radioMode
        info.radioStationName = newInfo.radioStationName
        info.playbackStatus = newInfo.playbackStatus
        info.sourceType = newInfo.sourceType
        info.launchIntent = newInfo.launchIntent
        info.playerIntent = newInfo.playerIntent
        info.iconUri = newInfo.iconUri
        info.currentProgress = newInfo.currentProgress
        info.artworkPath = newInfo.artworkPath
    }

    fun updateProgress(position: Long) {
        info.currentProgress = position
    }

    private val binder = object : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val transactionName = when (code) {
                TRANSACTION_onPlay -> "onPlay"
                TRANSACTION_onPause -> "onPause"
                TRANSACTION_onNext -> "onNext"
                TRANSACTION_onPrevious -> "onPrevious"
                TRANSACTION_onForward -> "onForward"
                TRANSACTION_onRewind -> "onRewind"
                TRANSACTION_getMusicPlaybackInfo -> "getMusicPlaybackInfo"
                else -> "unknown"
            }
            Log.w(TAG, "onTransact: code=$code ($transactionName)")
            when (code) {
                INTERFACE_TRANSACTION -> {
                    reply?.writeString("ecarx.xsf.mediacenter.IMusicClient")
                    return true
                }
                TRANSACTION_getMusicPlaybackInfo -> {
                    data.enforceInterface(DESCRIPTOR)
                    val playbackInfo = getPlaybackInfo()
                    reply?.writeNoException()
                    reply?.writeStrongBinder(playbackInfo.asBinder())
                    return true
                }
                TRANSACTION_onNext -> {
                    data.enforceInterface(DESCRIPTOR)
                    sendMediaKey(KEYCODE_MEDIA_NEXT, true)
                    sendMediaKey(KEYCODE_MEDIA_NEXT, false)
                    reply?.writeNoException()
                    reply?.writeInt(1)
                    return true
                }
                TRANSACTION_onPrevious, TRANSACTION_onReplay -> {
                    data.enforceInterface(DESCRIPTOR)
                    sendMediaKey(KEYCODE_MEDIA_PREVIOUS, true)
                    sendMediaKey(KEYCODE_MEDIA_PREVIOUS, false)
                    reply?.writeNoException()
                    reply?.writeInt(1)
                    return true
                }
                TRANSACTION_onPause -> {
                    data.enforceInterface(DESCRIPTOR)
                    // Use explicit PAUSE, not toggle, to ensure correct state sync with System
                    executeTransportControl(pause = true)
                    reply?.writeNoException()
                    reply?.writeInt(1)
                    return true
                }
                TRANSACTION_onPlay -> {
                    data.enforceInterface(DESCRIPTOR)
                    // Use explicit PLAY, not toggle. Handle onPlay to fix "never starts again" issue.
                    executeTransportControl(pause = false)
                    reply?.writeNoException()
                    reply?.writeInt(1)
                    return true
                }
                TRANSACTION_onForward -> {
                    data.enforceInterface(DESCRIPTOR)
                    handleForwardRewind(KEYCODE_MEDIA_FORWARD)
                    reply?.writeNoException()
                    reply?.writeInt(1)
                    return true
                }
                TRANSACTION_onRewind -> {
                    data.enforceInterface(DESCRIPTOR)
                    handleForwardRewind(KEYCODE_MEDIA_REWIND)
                    reply?.writeNoException()
                    reply?.writeInt(1)
                    return true
                }
                else -> return super.onTransact(code, data, reply, flags)
            }
        }
    }

    private fun startKeyRepeat(keyCode: Int) {
        currentKeyCode = keyCode
        lastEventTime = System.currentTimeMillis()

        if (repeatRunnable == null) {
            repeatRunnable = object : Runnable {
                override fun run() {
                    if (System.currentTimeMillis() - lastEventTime > INACTIVITY_TIMEOUT) {
                        stopKeyRepeat()
                        return
                    }

                    sendMediaKey(currentKeyCode, true)
                    handler.postDelayed(this, REPEAT_DELAY_MS)
                }
            }
            handler.post(repeatRunnable!!)
        } else {
            lastEventTime = System.currentTimeMillis()
        }
    }

    private fun stopKeyRepeat() {
        repeatRunnable?.let {
            handler.removeCallbacks(it)
            repeatRunnable = null
        }
        sendMediaKey(currentKeyCode, false)
    }

    private fun handleForwardRewind(keyCode: Int) {
        when (keyCode) {
            KEYCODE_MEDIA_FORWARD, KEYCODE_MEDIA_REWIND -> startKeyRepeat(keyCode)
        }
    }

    fun sendMediaKey(keyCode: Int, press: Boolean) {
        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        val action = if (press) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP
        try {
            audioManager.dispatchMediaKeyEvent(KeyEvent(action, keyCode))
            Log.i(TAG, "Sent ${if (press) "press" else "release"} for key $keyCode")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending key $keyCode: $e")
        }
    }

    private fun executeTransportControl(pause: Boolean) {
        try {
            val mediaSessionManager = context.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            val controllers = mediaSessionManager.getActiveSessions(null)

            val targetController = controllers.firstOrNull { controller ->
                val pkg = controller.packageName ?: return@firstOrNull false
                !pkg.startsWith("com.android.bluetooth") &&
                !pkg.startsWith("com.ecarx.mediacenter") &&
                !pkg.startsWith("com.flyme")
            }

            if (targetController != null) {
                val state = targetController.playbackState?.state
                Log.i(TAG, "Executing ${if (pause) "PAUSE" else "PLAY"} for ${targetController.packageName}, current state=$state")

                if (pause) {
                    if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING) {
                        targetController.transportControls.pause()
                        Log.i(TAG, "Sent pause()")
                    } else {
                        Log.d(TAG, "Already paused or not playing, ignoring pause()")
                    }
                } else {
                    if (state != PlaybackState.STATE_PLAYING) {
                        targetController.transportControls.play()
                        Log.i(TAG, "Sent play()")
                    } else {
                        Log.d(TAG, "Already playing, ignoring play()")
                    }
                }
            } else {
                Log.w(TAG, "No suitable controller found, falling back to KeyEvent")
                // Fallback: send explicit key code if possible, or toggle if we can't distinguish
                // Since we want explicit, we can send PLAY or PAUSE keys specifically if API >= 11
                // But dispatchMediaKeyEvent takes a KeyEvent. 
                // KEYCODE_MEDIA_PLAY and KEYCODE_MEDIA_PAUSE are available.
                val key = if (pause) KeyEvent.KEYCODE_MEDIA_PAUSE else KeyEvent.KEYCODE_MEDIA_PLAY
                sendMediaKey(key, true)
                sendMediaKey(key, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error using TransportControls, falling back to KeyEvent", e)
            val key = if (pause) KeyEvent.KEYCODE_MEDIA_PAUSE else KeyEvent.KEYCODE_MEDIA_PLAY
            sendMediaKey(key, true)
            sendMediaKey(key, false)
        }
    }

    override fun asBinder(): IBinder = binder

    override fun getPlaybackInfo(): IMusicPlaybackInfo {
        return info
    }

    companion object {
        private const val TAG = "MusicClient"
        const val DESCRIPTOR = "ecarx.xsf.mediacenter.IMusicClient"
        const val TRANSACTION_onPlay = 1
        const val TRANSACTION_onPause = 2
        const val TRANSACTION_onNext = 3
        const val TRANSACTION_onPrevious = 4
        const val TRANSACTION_onForward = 5
        const val TRANSACTION_onRewind = 6
        const val TRANSACTION_onSourceSelected = 8
        const val TRANSACTION_onMediaSelected = 9
        const val TRANSACTION_getMusicPlaybackInfo = 10
        const val TRANSACTION_getCurrentSourceType = 12
        const val TRANSACTION_getCurrentProgress = 13
        const val TRANSACTION_onSourceChanged = 17
        const val TRANSACTION_onReplay = 18
        const val TRANSACTION_onMediaSelectedPlay = 21
        const val TRANSACTION_onMediaForward = 22
        const val TRANSACTION_onMediaCenterFocusChanged = 25
        const val TRANSACTION_onExit = 26
        const val TRANSACTION_operationType = 33

        const val KEYCODE_MEDIA_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS
        const val KEYCODE_MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT
        const val KEYCODE_MEDIA_CONFIRM = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        const val KEYCODE_MEDIA_FORWARD = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
        const val KEYCODE_MEDIA_REWIND = KeyEvent.KEYCODE_MEDIA_REWIND
        const val REPEAT_DELAY_MS = 100L
        const val INACTIVITY_TIMEOUT = 600L
    }
}

interface IMusicClient {
    fun getPlaybackInfo(): IMusicPlaybackInfo
    fun asBinder(): IBinder
}
