package com.ggscc.app.media

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.media.session.MediaButtonReceiver
import ecarx.xsf.mediacenter.MusicPlaybackInfo
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object MetadataAdapter {
    private const val TAG = "MetadataAdapter"

    fun toPlaybackInfo(controller: MediaController, context: Context): MusicPlaybackInfo {
        return MusicPlaybackInfo().apply {
            val meta = controller.metadata ?: return@apply
            val state = controller.playbackState ?: return@apply
            val pkg = controller.packageName ?: "unknown"

            appName = getAppName(pkg, context)
            packageName = pkg
            iconUri = getAppIconUri(pkg, context)
            
            // Map playback state correctly:
            // 1 = Playing
            // 2 = Paused / Stopped
            // 0 = None / Error (avoids using 0 for valid paused states)
            playbackStatus = when (state.state) {
                PlaybackState.STATE_PLAYING,
                PlaybackState.STATE_BUFFERING,
                PlaybackState.STATE_CONNECTING -> 1
                PlaybackState.STATE_PAUSED,
                PlaybackState.STATE_STOPPED -> 2
                else -> 0
            }

            title = meta.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
            artist = meta.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty()
            album = meta.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty()
            duration = meta.getLong(MediaMetadata.METADATA_KEY_DURATION)

            // Force Music type (6) for known music apps or standard tracks
            if (pkg.contains("spotify") || pkg.contains("yandex") || pkg.contains("music")) {
                sourceType = 6
            } else if (duration == -1L) {
                // Likely radio
                sourceType = 12
                radioStationName = meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION).orEmpty()
            } else {
                sourceType = 6
            }

            meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)?.let {
                if (it.endsWith(".lrc")) lyric = it.toUri()
            }

            lyricContent = meta.getString("android.media.metadata.LYRICS").orEmpty()
            lyricSentence = meta.getString("android.media.metadata.CURRENT_LYRIC").orEmpty()

            artwork = loadArtworkUri(meta, context)
            artworkPath = if (artwork != Uri.EMPTY) artwork.path ?: "" else ""

            // Fallback to package launch intent if session activity is null
            launchIntent = controller.sessionActivity ?: context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }
            
            playerIntent = createMediaButtonPendingIntent(context)
        }
    }

    private fun getAppName(packageName: String, context: Context): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun loadArtworkUri(meta: MediaMetadata, context: Context): Uri {
        // First try Bitmap (most reliable if present)
        listOf(
            MediaMetadata.METADATA_KEY_ALBUM_ART,
            MediaMetadata.METADATA_KEY_ART
        ).forEach { key ->
            meta.getBitmap(key)?.let { bitmap ->
                return cacheBitmap(context, generateArtworkFilename(meta), bitmap)
            }
        }

        // Then try URI
        listOf(
            MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
            MediaMetadata.METADATA_KEY_ART_URI
        ).forEach { key ->
            meta.getString(key)?.let { uriString ->
                Log.d(TAG, "Got URI from $key: $uriString")
                // If it's a content URI, pass it through.
                // If it's http, pass it through (car might not support it, but better than nothing)
                return uriString.toUri()
            }
        }

        return Uri.EMPTY
    }

    private fun getAppIconUri(packageName: String, context: Context): String {
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val iconDrawable = appInfo.loadIcon(pm)
            val bitmap = drawableToBitmap(iconDrawable)
            val uri = cacheBitmap(context, "icon_$packageName.png", bitmap)
            return uri.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app icon for $packageName", e)
            return ""
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 96,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 96,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @SuppressLint("SetWorldReadable", "SetWorldWritable")
    private fun cacheBitmap(context: Context, filename: String, bmp: Bitmap): Uri {
        val dir = File(context.externalCacheDir, "art").apply { mkdirs() }
        val file = File(dir, filename)

        // Always overwrite if it's the icon to ensure freshness, or check existence
        if (!file.exists()) {
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            // Explicitly set world readable for direct file access compatibility
            file.setReadable(true, false)
            file.setWritable(true, false)
            file.setExecutable(false, false)
            Log.d(TAG, "Cached bitmap: ${file.absolutePath}")
        }

        // Return raw file URI instead of FileProvider content URI
        // This is required for legacy/OEM implementations that expect direct file paths
        return Uri.fromFile(file)
    }

    private fun generateArtworkFilename(meta: MediaMetadata): String {
        val uniqueId = buildString {
            append(meta.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) ?: "")
            append(meta.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "")
            append(meta.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "")
            append(meta.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: "")
        }

        return if (uniqueId.isNotEmpty()) {
            val hash = md5(uniqueId)
            "art_$hash.jpg"
        } else {
            "art_${System.currentTimeMillis()}.jpg"
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun createMediaButtonPendingIntent(ctx: Context): PendingIntent =
        PendingIntent.getBroadcast(
            ctx,
            0,
            Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                component = ComponentName(ctx, MediaButtonReceiver::class.java)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
}
