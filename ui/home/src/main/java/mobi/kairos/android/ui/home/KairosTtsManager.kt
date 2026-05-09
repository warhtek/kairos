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
    private var currentText: String = ""
    private var currentWordStart: Int = -1
    private var currentWordEnd: Int = -1

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
            val currentLocale = getCurrentLocale()
            tts?.language = currentLocale
            Log.d("KairosTtsManager", "TTS language set to: ${currentLocale.displayName}")

            restoreSavedSettings()
            isReady = true

            loadAvailableVoices()
            restoreSavedVoice()

            setupUtteranceListener()

            Log.d("KairosTtsManager", "TTS ready")
        } else {
            Log.e("KairosTtsManager", "TTS init failed")
        }
    }

    private fun getCurrentLocale(): Locale {
        val currentLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
        return if (currentLocale.language == "es") {
            Locale("es", "ES")
        } else {
            currentLocale
        }
    }

    private fun loadAvailableVoices() {
        val allVoices = tts?.voices ?: emptyList()
        val currentLanguage = getCurrentLocale().language

        availableVoices = allVoices.sortedWith(compareBy<Voice> {
            !it.locale.language.equals(currentLanguage, ignoreCase = true)
        }.thenBy { it.name })
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("KairosTtsManager", "onStart: $utteranceId")
                isPlaying = true
                currentWordStart = -1
                currentWordEnd = -1
                onPlayingChanged?.invoke(true)
            }

            override fun onDone(utteranceId: String?) {
                Log.d("KairosTtsManager", "onDone: $utteranceId")
                isPlaying = false
                onPlayingChanged?.invoke(false)
                onRangeStart?.invoke(-1, -1)
            }

            override fun onError(utteranceId: String?) {
                Log.e("KairosTtsManager", "onError: $utteranceId")
                isPlaying = false
                onPlayingChanged?.invoke(false)
                onRangeStart?.invoke(-1, -1)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e("KairosTtsManager", "onError: $utteranceId, code=$errorCode")
                isPlaying = false
                onPlayingChanged?.invoke(false)
                onRangeStart?.invoke(-1, -1)
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                // Los offsets son globales al texto completo
                Log.d("KairosTtsManager", "onRangeStart: start=$start, end=$end")
                onRangeStart?.invoke(start, end)
            }
        })
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
            Log.e("KairosTtsManager", "TTS not ready, cannot speak")
            return
        }

        if (text.isBlank()) {
            Log.w("KairosTtsManager", "Empty text to speak")
            return
        }

        stop()

        currentText = text

        // Hablar el texto completo de una vez, sin dividir en chunks
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
        currentText = ""
        onPlayingChanged?.invoke(false)
        onRangeStart?.invoke(-1, -1)
    }

    fun setVoice(voice: Voice) {
        currentVoice = voice
        tts?.voice = voice
        preferencesRepo.saveSelectedVoice(voice.name)
        Log.d("KairosTtsManager", "Voice set to: ${voice.name}")
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
        preferencesRepo.saveSpeechRate(rate)
        Log.d("KairosTtsManager", "Speech rate set to: $rate")
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
        preferencesRepo.savePitch(pitch)
        Log.d("KairosTtsManager", "Pitch set to: $pitch")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        instance = null
    }

    fun isReady(): Boolean = isReady

    fun refreshLanguage() {
        if (isReady) {
            val currentLocale = getCurrentLocale()
            tts?.language = currentLocale
            loadAvailableVoices()
            restoreSavedVoice()
            Log.d("KairosTtsManager", "Language refreshed to: ${currentLocale.displayName}")
        }
    }
}
