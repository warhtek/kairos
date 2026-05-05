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
    private var currentChunks = mutableListOf<String>()
    private var currentChunkIndex = 0
    private var isSpeakingChunks = false
    private var currentAppLanguage: String = "es" // Idioma actual de la app

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
            // Obtener el idioma actual del dispositivo/configuración
            val currentLocale = getCurrentLocale()
            tts?.language = currentLocale
            Log.d("KairosTtsManager", "TTS language set to: ${currentLocale.displayName}")

            restoreSavedSettings()
            isReady = true

            // Cargar todas las voces disponibles sin filtrar por idioma
            loadAvailableVoices()

            restoreSavedVoice()

            // Configurar el listener principal
            setupUtteranceListener()

            Log.d("KairosTtsManager", "TTS ready, voices: ${availableVoices.size}")
        } else {
            Log.e("KairosTtsManager", "TTS init failed with status: $status")
        }
    }

    private fun getCurrentLocale(): Locale {
        // Obtener el idioma actual del sistema/dispositivo
        val currentLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }

        // Si el idioma es español, usar español de España por defecto
        return if (currentLocale.language == "es") {
            Locale("es", "ES")
        } else {
            currentLocale
        }
    }

    private fun loadAvailableVoices() {
        val allVoices = tts?.voices ?: emptyList()

        // Obtener el idioma actual para priorizar
        val currentLanguage = getCurrentLocale().language

        // Ordenar voces: primero las del idioma actual, luego el resto
        availableVoices = allVoices.sortedWith(compareBy<Voice> {
            !it.locale.language.equals(currentLanguage, ignoreCase = true)
        }.thenBy { it.name })

        Log.d("KairosTtsManager", "Available voices: ${availableVoices.map { "${it.name} (${it.locale.displayName})" }}")
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("KairosTtsManager", "onStart: $utteranceId")
                isPlaying = true
                onPlayingChanged?.invoke(true)
            }

            override fun onDone(utteranceId: String?) {
                Log.d("KairosTtsManager", "onDone: $utteranceId")

                if (isSpeakingChunks) {
                    currentChunkIndex++
                    if (currentChunkIndex < currentChunks.size) {
                        speakCurrentChunk()
                    } else {
                        isSpeakingChunks = false
                        isPlaying = false
                        currentChunks.clear()
                        currentChunkIndex = 0
                        onPlayingChanged?.invoke(false)
                        onRangeStart?.invoke(-1, -1)
                    }
                } else {
                    isPlaying = false
                    onPlayingChanged?.invoke(false)
                    onRangeStart?.invoke(-1, -1)
                }
            }

            override fun onError(utteranceId: String?) {
                Log.e("KairosTtsManager", "onError (1 param): $utteranceId")
                handlePlaybackError()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e("KairosTtsManager", "onError (2 params): $utteranceId, code=$errorCode")
                handlePlaybackError()
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                onRangeStart?.invoke(start, end)
            }
        })
    }

    private fun handlePlaybackError() {
        if (isSpeakingChunks) {
            currentChunkIndex++
            if (currentChunkIndex < currentChunks.size) {
                speakCurrentChunk()
            } else {
                isSpeakingChunks = false
                isPlaying = false
                currentChunks.clear()
                currentChunkIndex = 0
                onPlayingChanged?.invoke(false)
                onRangeStart?.invoke(-1, -1)
            }
        } else {
            isPlaying = false
            onPlayingChanged?.invoke(false)
            onRangeStart?.invoke(-1, -1)
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

    private fun splitTextIntoChunks(text: String, maxChunkSize: Int = 500): List<String> {
        val chunks = mutableListOf<String>()

        val sentences = text.split(Regex("(?<=[.!?;])\\s+"))
        var currentChunk = StringBuilder()

        for (sentence in sentences) {
            if (currentChunk.length + sentence.length + 1 <= maxChunkSize) {
                if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                currentChunk.append(sentence)
            } else {
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString())
                }
                if (sentence.length > maxChunkSize) {
                    val words = sentence.split(" ")
                    var wordChunk = StringBuilder()
                    for (word in words) {
                        if (wordChunk.length + word.length + 1 <= maxChunkSize) {
                            if (wordChunk.isNotEmpty()) wordChunk.append(" ")
                            wordChunk.append(word)
                        } else {
                            if (wordChunk.isNotEmpty()) {
                                chunks.add(wordChunk.toString())
                            }
                            wordChunk = StringBuilder(word)
                        }
                    }
                    if (wordChunk.isNotEmpty()) {
                        chunks.add(wordChunk.toString())
                    }
                    currentChunk = StringBuilder()
                } else {
                    currentChunk = StringBuilder(sentence)
                }
            }
        }
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString())
        }

        if (chunks.isEmpty() && text.isNotEmpty()) {
            chunks.add(text)
        }

        Log.d("KairosTtsManager", "Split text into ${chunks.size} chunks")
        return chunks
    }

    private fun speakCurrentChunk() {
        if (currentChunkIndex >= currentChunks.size) {
            isSpeakingChunks = false
            isPlaying = false
            currentChunks.clear()
            currentChunkIndex = 0
            onPlayingChanged?.invoke(false)
            onRangeStart?.invoke(-1, -1)
            return
        }

        val chunk = currentChunks[currentChunkIndex]
        val utteranceId = "kairos_chunk_${currentChunkIndex}_${System.currentTimeMillis()}"
        Log.d("KairosTtsManager", "Speaking chunk ${currentChunkIndex + 1}/${currentChunks.size}")

        tts?.speak(chunk, TextToSpeech.QUEUE_ADD, null, utteranceId)
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

        if (text.length > 500) {
            currentChunks = splitTextIntoChunks(text).toMutableList()
            currentChunkIndex = 0
            isSpeakingChunks = true

            if (currentChunks.isNotEmpty()) {
                speakCurrentChunk()
            } else {
                Log.e("KairosTtsManager", "No chunks to speak")
            }
        } else {
            isSpeakingChunks = false
            val utteranceId = "kairos_utterance_${System.currentTimeMillis()}"
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

            if (result != TextToSpeech.SUCCESS) {
                Log.e("KairosTtsManager", "Speak failed with result: $result")
                isPlaying = false
                onPlayingChanged?.invoke(false)
            }
        }
    }

    fun stop() {
        Log.d("KairosTtsManager", "stop called")
        tts?.stop()
        isPlaying = false
        isSpeakingChunks = false
        currentChunks.clear()
        currentChunkIndex = 0
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

    // Método para refrescar el idioma cuando cambia la configuración de la app
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
