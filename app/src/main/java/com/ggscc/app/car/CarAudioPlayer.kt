package com.ggscc.app.car

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CarAudioPlayer(private val context: Context) {
    private var playbackThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var isPlaying = false

    @Volatile
    var gainPercent: Int = 100

    companion object {
        private const val TAG = "CarAudioPlayer"
        private const val CAR_EXTERNAL_USAGE = 29
    }

    fun playFromAssets(assetPath: String) {
        stop()

        playbackThread = Thread {
            var extractor: MediaExtractor? = null
            var codec: MediaCodec? = null

            try {
                extractor = MediaExtractor()
                val afd = context.assets.openFd(assetPath)
                extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                val audioTrackIndex = (0 until extractor.trackCount).firstOrNull { i ->
                    extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
                } ?: return@Thread

                extractor.selectTrack(audioTrackIndex)
                val format = extractor.getTrackFormat(audioTrackIndex)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: return@Thread
                val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

                codec = MediaCodec.createDecoderByType(mime)
                codec.configure(format, null, null, 0)
                codec.start()

                val channelConfig = if (channelCount == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
                val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(CAR_EXTERNAL_USAGE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()
                isPlaying = true

                val bufferInfo = MediaCodec.BufferInfo()
                var sawInputEOS = false
                var sawOutputEOS = false

                while (!sawOutputEOS && isPlaying) {
                    if (!sawInputEOS) {
                        val inputBufferIndex = codec.dequeueInputBuffer(10000)
                        if (inputBufferIndex >= 0) {
                            val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                            if (inputBuffer != null) {
                                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                                if (sampleSize < 0) {
                                    codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                    sawInputEOS = true
                                } else {
                                    codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                                    extractor.advance()
                                }
                            }
                        }
                    }

                    val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                    if (outputBufferIndex >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            val pcmData = ByteArray(bufferInfo.size)
                            outputBuffer.get(pcmData)
                            outputBuffer.clear()

                            if (gainPercent != 100) {
                                applyGain(pcmData, gainPercent)
                            }

                            audioTrack?.write(pcmData, 0, pcmData.size)
                        }
                        codec.releaseOutputBuffer(outputBufferIndex, false)

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            sawOutputEOS = true
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error playing sound: $assetPath", e)
            } finally {
                isPlaying = false
                try { codec?.stop() } catch (_: Exception) {}
                try { codec?.release() } catch (_: Exception) {}
                try { extractor?.release() } catch (_: Exception) {}
                try { audioTrack?.stop() } catch (_: Exception) {}
                try { audioTrack?.release() } catch (_: Exception) {}
                audioTrack = null
            }
        }.apply {
            name = "AudioPlaybackThread"
            start()
        }
    }

    private fun applyGain(pcmData: ByteArray, percent: Int) {
        val gain = percent / 100f
        val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)
        val shortBuffer = buffer.asShortBuffer()

        for (i in 0 until shortBuffer.capacity()) {
            val sample = shortBuffer.get(i)
            val amplified = (sample * gain).toInt()
            shortBuffer.put(i, amplified.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())
        }
    }

    fun stop() {
        isPlaying = false
        try {
            playbackThread?.join(500)
        } catch (_: InterruptedException) {}
        playbackThread = null
    }

    fun release() {
        stop()
    }
}
