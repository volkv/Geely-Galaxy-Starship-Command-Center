package com.ggscc.app.car

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log

class LoudspeakerController {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var streamingThread: Thread? = null

    @Volatile
    private var isStreaming = false

    @Volatile
    var gainPercent: Int = 100

    companion object {
        private const val TAG = "LoudspeakerController"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val CAR_EXTERNAL_USAGE = 29
    }

    fun start(): Boolean {
        if (isStreaming) {
            Log.w(TAG, "Already streaming")
            return true
        }

        try {
            val minBufferSizeRecord = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT
            )
            val minBufferSizeTrack = AudioTrack.getMinBufferSize(
                SAMPLE_RATE, CHANNEL_CONFIG_OUT, AUDIO_FORMAT
            )

            if (minBufferSizeRecord == AudioRecord.ERROR || minBufferSizeRecord == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid AudioRecord buffer size")
                return false
            }

            val bufferSize = maxOf(minBufferSizeRecord, minBufferSizeTrack, 4096)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG_IN,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                release()
                return false
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(CAR_EXTERNAL_USAGE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG_OUT)
                .setEncoding(AUDIO_FORMAT)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                Log.e(TAG, "AudioTrack initialization failed")
                release()
                return false
            }

            isStreaming = true
            audioRecord?.startRecording()
            audioTrack?.play()

            streamingThread = Thread {
                val buffer = ShortArray(bufferSize / 2)

                while (isStreaming) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: -1

                    if (readCount > 0) {
                        if (gainPercent != 100) {
                            applyGain(buffer, readCount, gainPercent)
                        }
                        audioTrack?.write(buffer, 0, readCount)
                    } else if (readCount < 0) {
                        Log.e(TAG, "AudioRecord read error: $readCount")
                        break
                    }
                }

                Log.d(TAG, "Streaming thread finished")
            }.apply {
                name = "LoudspeakerThread"
                start()
            }

            Log.i(TAG, "Loudspeaker started with usage=$CAR_EXTERNAL_USAGE")
            return true

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for AudioRecord", e)
            release()
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error starting loudspeaker", e)
            release()
            return false
        }
    }

    fun stop() {
        isStreaming = false

        try {
            streamingThread?.join(1000)
        } catch (e: InterruptedException) {
            Log.w(TAG, "Thread join interrupted")
        }
        streamingThread = null

        release()
        Log.i(TAG, "Loudspeaker stopped")
    }

    private fun release() {
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioRecord", e)
        }
        audioRecord?.release()
        audioRecord = null

        try {
            audioTrack?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioTrack", e)
        }
        audioTrack?.release()
        audioTrack = null
    }

    fun isActive(): Boolean = isStreaming

    private fun applyGain(buffer: ShortArray, count: Int, percent: Int) {
        val gain = percent / 100f
        for (i in 0 until count) {
            val amplified = (buffer[i] * gain).toInt()
            buffer[i] = amplified.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }
}
