package com.example.service

import android.media.AudioFormat
import android.media.AudioAttributes
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random

enum class AmbientSoundType(val displayName: String, val icon: String, val category: String) {
    OFF("Off", "🔇", "Control"),
    RAIN("Heavy Rain", "🌧️", "Nature"),
    THUNDER("Thunderstorm", "⛈️", "Nature"),
    LOFI("Lofi Chill Beats", "🎧", "Music"),
    COFFEE("Coffee Shop", "☕", "Urban"),
    FOREST("Forest Wind", "🌲", "Nature"),
    OCEAN("Ocean Waves", "🌊", "Nature"),
    FIREPLACE("Cozy Fireplace", "🔥", "Cozy"),
    WHITE_NOISE("Calm White Noise", "📻", "Focus")
}

object AmbientSoundPlayer {
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    private var currentType = AmbientSoundType.OFF

    fun startSound(type: AmbientSoundType) {
        stopSound()
        if (type == AmbientSoundType.OFF) return
        currentType = type

        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()

        playJob = CoroutineScope(Dispatchers.Default).launch {
            val buffer = ShortArray(minBufferSize)
            val random = Random()
            var phase = 0.0

            while (isActive) {
                for (i in buffer.indices) {
                    when (type) {
                        AmbientSoundType.RAIN -> {
                            // Pink/Brown noise filtered for rain
                            val noise = random.nextFloat() * 2f - 1f
                            buffer[i] = (noise * 0.15f * 32767).toInt().toShort()
                        }
                        AmbientSoundType.THUNDER -> {
                            val noise = random.nextFloat() * 2f - 1f
                            val rumble = Math.sin(phase) * 0.2
                            phase += 0.01
                            buffer[i] = ((noise * 0.12f + rumble) * 32767).toInt().coerceIn(-32768, 32767).toShort()
                        }
                        AmbientSoundType.COFFEE -> {
                            val chatter = Math.sin(phase) * Math.cos(phase * 0.5) * 0.1
                            val noise = (random.nextFloat() * 2f - 1f) * 0.08f
                            phase += 0.03
                            buffer[i] = ((chatter + noise) * 32767).toInt().coerceIn(-32768, 32767).toShort()
                        }
                        AmbientSoundType.FOREST -> {
                            val wind = Math.sin(phase) * 0.15
                            phase += 0.005
                            val whisper = (random.nextFloat() * 2f - 1f) * 0.05f
                            buffer[i] = ((wind + whisper) * 32767).toInt().coerceIn(-32768, 32767).toShort()
                        }
                        AmbientSoundType.LOFI -> {
                            // Warm Lofi Sine Chord Sweep (220Hz / 277Hz / 330Hz)
                            val f1 = 220.0
                            val f2 = 277.18
                            val val1 = Math.sin(2.0 * Math.PI * f1 * phase) * 0.08
                            val val2 = Math.sin(2.0 * Math.PI * f2 * phase) * 0.08
                            phase += 1.0 / sampleRate
                            buffer[i] = ((val1 + val2) * 32767).toInt().coerceIn(-32768, 32767).toShort()
                        }
                        AmbientSoundType.OCEAN -> {
                            val wave = Math.sin(phase * 0.002) * 0.2f
                            phase += 0.01
                            val noise = (random.nextFloat() * 2f - 1f) * (0.05f + Math.abs(wave.toFloat()) * 0.1f)
                            buffer[i] = ((wave + noise) * 32767).toInt().coerceIn(-32768, 32767).toShort()
                        }
                        AmbientSoundType.FIREPLACE -> {
                            val crackle = if (random.nextFloat() < 0.02f) (random.nextFloat() * 0.4f - 0.2f) else 0f
                            val hum = Math.sin(phase) * 0.05
                            phase += 0.01
                            buffer[i] = ((crackle + hum) * 32767).toInt().coerceIn(-32768, 32767).toShort()
                        }
                        AmbientSoundType.WHITE_NOISE -> {
                            val noise = (random.nextFloat() * 2f - 1f) * 0.1f
                            buffer[i] = (noise * 32767).toInt().toShort()
                        }
                        else -> buffer[i] = 0
                    }
                }
                track.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopSound() {
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
        currentType = AmbientSoundType.OFF
    }

    fun getCurrentType(): AmbientSoundType = currentType
}
