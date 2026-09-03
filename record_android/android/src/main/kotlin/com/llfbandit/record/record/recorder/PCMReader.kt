package com.llfbandit.record.record.recorder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaFormat
import android.util.Log
import com.llfbandit.record.record.audio_manager.AudioEffectsManager
import com.llfbandit.record.record.model.RecordConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.log10

class PCMReader(
  private val config: RecordConfig,
  private val mediaFormat: MediaFormat,
) : AutoCloseable {
  companion object {
    private val TAG = PCMReader::class.java.simpleName
    private const val DEFAULT_AMPLITUDE_DB = -160.0
    private const val MAX_PCM_16_VALUE = 32767.0 // 2^15 - 1 for 16-bit signed
  }

  private val audioFormat: Int = getAudioFormat()
  private val bufferSize: Int = initBufferSize()
  private val shortBuffer: ShortArray? =
    if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) ShortArray(bufferSize / 2) else null
  private val floatBuffer: FloatArray? =
    if (audioFormat == AudioFormat.ENCODING_PCM_FLOAT) FloatArray(bufferSize / 4) else null
  private val reader: AudioRecord = createReader()
  private val effects: AudioEffectsManager =
    AudioEffectsManager(reader.audioSessionId).also { it.apply(config) }

  private var amplitudeDb: Double = DEFAULT_AMPLITUDE_DB

  fun start() {
    reader.startRecording()
  }

  fun stop() {
    if (reader.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
      reader.stop()
    }
  }

  @Throws(Exception::class)
  fun read(): ByteArray {
    val readResult = when (audioFormat) {
      AudioFormat.ENCODING_PCM_FLOAT -> {
        val buffer = requireNotNull(floatBuffer)
        reader.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
      }
      else -> {
        val buffer = requireNotNull(shortBuffer)
        reader.read(buffer, 0, buffer.size)
      }
    }
    if (readResult < 0) {
      throw Exception(getReadFailureReason(readResult))
    }

    if (readResult > 0) {
      amplitudeDb = calculateAmplitudeDb(readResult)
    }

    return convertToByteArray(readResult)
  }

  fun getAmplitude(): Double = amplitudeDb

  override fun close() {
    release()
  }

  fun release() {
    stop()
    effects.release()
    reader.release()
  }

  @SuppressLint("MissingPermission")
  @Throws(Exception::class)
  private fun createReader(): AudioRecord {
    val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
    val channels = getChannelsConfig()
    val audioFormat = getAudioFormat()

    val reader = try {
      AudioRecord(
        config.audioSource,
        sampleRate,
        channels,
        audioFormat,
        bufferSize
      )
    } catch (e: IllegalArgumentException) {
      throw Exception("Unable to instantiate PCM reader.", e)
    }

    if (reader.state != AudioRecord.STATE_INITIALIZED) {
      reader.release()
      throw Exception("PCM reader failed to initialize.")
    }

    if (config.device != null) {
      if (!reader.setPreferredDevice(config.device)) {
        Log.w(TAG, "Unable to set device: ${config.device.productName}")
      }
    }

    return reader
  }

  private fun initBufferSize(): Int {
    val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
    val channels = getChannelsConfig()
    val audioFormat = getAudioFormat()
    return config.streamBufferSize ?: calculateBufferSize(sampleRate, channels, audioFormat)
  }

  @Throws(Exception::class)
  private fun calculateBufferSize(sampleRate: Int, channelConfig: Int, audioFormat: Int): Int {
    val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    return when {
      minBufferSize == AudioRecord.ERROR_BAD_VALUE || minBufferSize == AudioRecord.ERROR -> {
        throw Exception("Recording config is not supported by the hardware, or an invalid config was provided.")
      }

      else -> minBufferSize * 2 // Double the minimum buffer size for safety margin
    }
  }

  private fun getAudioFormat(): Int =
    if (mediaFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
      mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
    } else {
      AudioFormat.ENCODING_PCM_16BIT
    }

  private fun getChannelsConfig(): Int {
    val numChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

    return if (numChannels == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
  }

  private fun convertToByteArray(size: Int): ByteArray {
    val bytesPerSample = if (audioFormat == AudioFormat.ENCODING_PCM_FLOAT) 4 else 2
    val byteBuffer = ByteBuffer.allocate(size * bytesPerSample).order(ByteOrder.LITTLE_ENDIAN)
    when (audioFormat) {
      AudioFormat.ENCODING_PCM_FLOAT -> {
        val buffer = requireNotNull(floatBuffer)
        for (i in 0 until size) byteBuffer.putFloat(buffer[i])
      }
      else -> {
        val buffer = requireNotNull(shortBuffer)
        for (i in 0 until size) byteBuffer.putShort(buffer[i])
      }
    }
    return byteBuffer.array()
  }

  private fun calculateAmplitudeDb(size: Int): Double {
    val normalizedMax = when (audioFormat) {
      AudioFormat.ENCODING_PCM_FLOAT ->
        requireNotNull(floatBuffer).take(size).maxOf { abs(it.toDouble()) }
      else ->
        requireNotNull(shortBuffer).take(size).maxOf { abs(it.toInt()) } / MAX_PCM_16_VALUE
    }
    if (normalizedMax == 0.0) return DEFAULT_AMPLITUDE_DB
    return 0.0.coerceAtMost(20 * log10(normalizedMax))
  }

  private fun getReadFailureReason(errorCode: Int): String {
    val message = when (errorCode) {
      AudioRecord.ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION: Failure due to improper method use"
      AudioRecord.ERROR_BAD_VALUE -> "ERROR_BAD_VALUE: Invalid value used"
      AudioRecord.ERROR_DEAD_OBJECT -> "ERROR_DEAD_OBJECT: Object no longer valid, needs recreation"
      AudioRecord.ERROR -> "ERROR: Generic operation failure"
      else -> "Unknown error code: $errorCode"
    }
    return "Error when reading audio data: $message"
  }
}
