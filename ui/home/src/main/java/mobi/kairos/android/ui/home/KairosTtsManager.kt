package mobi.kairos.android.ui.home

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale
import mobi.kairos.android.data.repository.TtsPreferencesRepository

class KairosTtsManager private constructor(
    private val context: Context,
    private val preferencesRepo: TtsPreferencesRepository
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    var availableVoices: List<Voice> = emptyList()
        private set

    var currentVoice: Voice? = null
        private set

    var isPlaying = false
        private set

    var onPlayingChanged: ((Boolean) -> Unit)? = null
    var onRangeStart: ((start: Int, end: Int) -> Unit)? = null

    companion object {
        @Volatile
        private var instance: KairosTtsManager? = null

        fun getInstance(context: Context, preferencesRepo: TtsPreferencesRepository): KairosTtsManager {
            return instance ?: synchronized(this) {
                instance ?: KairosTtsManager(context.applicationContext, preferencesRepo).also {
                    instance = it
                }
            }
        }
    }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        Log.d("KairosTtsManager", "onInit called with status: $status")
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")

            // Restaurar configuración guardada
            restoreSavedSettings()

            isReady = true

            availableVoices = tts?.voices
                ?.filter { voice ->
                    !voice.isNetworkConnectionRequired &&
                        (voice.locale.language == "es" || voice.locale.language == "spa")
                }
                ?.sortedBy { it.name }
                ?: emptyList()

            // Restaurar voz guardada si existe
            restoreSavedVoice()

            Log.d("KairosTtsManager", "TTS ready, voices: ${availableVoices.size}")
        } else {
            Log.e("KairosTtsManager", "TTS init failed with status: $status")
        }
    }

    private fun restoreSavedSettings() {
        val savedRate = preferencesRepo.getSpeechRate()
        val savedPitch = preferencesRepo.getPitch()
        tts?.setSpeechRate(savedRate)
        tts?.setPitch(savedPitch)
        Log.d("KairosTtsManager", "Restored settings - rate: $savedRate, pitch: $savedPitch")
    }

    private fun restoreSavedVoice() {
        val savedVoiceName = preferencesRepo.getSelectedVoice()
        if (savedVoiceName != null && availableVoices.isNotEmpty()) {
            val savedVoice = availableVoices.find { it.name == savedVoiceName }
            if (savedVoice != null) {
                currentVoice = savedVoice
                tts?.voice = savedVoice
                Log.d("KairosTtsManager", "Restored saved voice: ${savedVoice.name}")
            }
        }
    }

    fun speak(text: String) {
        Log.d("KairosTtsManager", "speak called, isReady=$isReady, text length=${text.length}")

        if (!isReady) {
            Log.w("KairosTtsManager", "TTS not ready, cannot speak")
            return
        }

        if (text.isBlank()) {
            Log.w("KairosTtsManager", "Empty text to speak")
            return
        }

        stop()

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("KairosTtsManager", "onStart: $utteranceId")
                isPlaying = true
                onPlayingChanged?.invoke(true)
            }

            override fun onDone(utteranceId: String?) {
                Log.d("KairosTtsManager", "onDone: $utteranceId")
                isPlaying = false
                onPlayingChanged?.invoke(false)
                onRangeStart?.invoke(-1, -1)
            }

            // Método onError con 1 parámetro (versiones antiguas)
            override fun onError(utteranceId: String?) {
                Log.e("KairosTtsManager", "onError (1 param): $utteranceId")
                isPlaying = false
                onPlayingChanged?.invoke(false)
                onRangeStart?.invoke(-1, -1)
            }

            // Método onError con 2 parámetros (versiones nuevas)
            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e("KairosTtsManager", "onError (2 params): $utteranceId, code=$errorCode")
                isPlaying = false
                onPlayingChanged?.invoke(false)
                onRangeStart?.invoke(-1, -1)
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                Log.d("KairosTtsManager", "onRangeStart: start=$start, end=$end")
                onRangeStart?.invoke(start, end)
            }
        })

        val utteranceId = "kairos_utterance_${System.currentTimeMillis()}"
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        if (result == TextToSpeech.SUCCESS) {
            Log.d("KairosTtsManager", "Speak started successfully")
        } else {
            Log.e("KairosTtsManager", "Speak failed with result: $result")
            isPlaying = false
            onPlayingChanged?.invoke(false)
        }
    }

    fun stop() {
        Log.d("KairosTtsManager", "stop called")
        tts?.stop()
        isPlaying = false
        onPlayingChanged?.invoke(false)
        onRangeStart?.invoke(-1, -1)
    }

    fun setVoice(voice: Voice) {
        currentVoice = voice
        tts?.voice = voice
        preferencesRepo.saveSelectedVoice(voice.name)
        Log.d("KairosTtsManager", "Voice set to: ${voice.name} and saved")
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
        preferencesRepo.saveSpeechRate(rate)
        Log.d("KairosTtsManager", "Speech rate set to: $rate and saved")
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
        preferencesRepo.savePitch(pitch)
        Log.d("KairosTtsManager", "Pitch set to: $pitch and saved")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        instance = null
    }

    fun isReady(): Boolean = isReady
}
