package com.ggscc.app.media

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.util.Log
import com.ggscc.app.car.MediaCenterBridge

class MediaControllerCallbackHandler(
    private val controller: MediaController,
    private val gateway: MediaCenterBridge,
    private val onSessionDestroyed: () -> Unit,
    private val onPlaybackStarted: (() -> Unit)? = null
) : MediaController.Callback() {

    private var isPlaying = false

    override fun onMetadataChanged(metadata: MediaMetadata?) {
        Log.i(TAG, "Metadata changed: ${metadata?.description?.title}")
        metadata?.let {
            updateBridgeWithMetadata(if (isPlaying) 1 else 0)
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackState?) {
        Log.d(TAG, "PlaybackState changed: ${state?.state}")
        state?.let {
            val newIsPlaying = when (state.state) {
                PlaybackState.STATE_PLAYING,
                PlaybackState.STATE_BUFFERING,
                PlaybackState.STATE_CONNECTING -> true
                PlaybackState.STATE_PAUSED,
                PlaybackState.STATE_STOPPED,
                PlaybackState.STATE_ERROR -> false
                else -> isPlaying
            }

            if (newIsPlaying != isPlaying) {
                isPlaying = newIsPlaying
                updateBridgeWithMetadata(isPlaying = if (isPlaying) 1 else 0)
                if (isPlaying) {
                    onPlaybackStarted?.invoke()
                }
            }

            if (isPlaying) {
                gateway.updateProgress(state.position)
            }
        }
    }

    override fun onSessionDestroyed() {
        Log.i(TAG, "Session destroyed")
        onSessionDestroyed.invoke()
    }

    private fun updateBridgeWithMetadata(isPlaying: Int) {
        controller.metadata?.let {
            val info = MetadataAdapter.toPlaybackInfo(controller, gateway.context)
            gateway.updateWithPlaybackInfo(info.apply { playbackStatus = isPlaying })
        }
    }

    companion object {
        private const val TAG = "MediaCallback"
    }
}
