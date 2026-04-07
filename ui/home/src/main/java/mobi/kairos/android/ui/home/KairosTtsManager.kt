package mobi.kairos.android.ui.home

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class KairosTtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isReady = false

    var availableVoices: List<Voice> = emptyList()
        private set

    var currentVoice: Voice? = null
        private set

    var isPlaying = false
        private set

    var onPlayingChanged: ((Boolean) -> Unit)? = null
    var onRangeStart: ((start: Int, end: Int) -> Unit)? = null

    override fun onInit(status: Int) {
        Log.d("KairosTtsManager", "onInit called with status: $status")
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("es", "ES")
            isReady = true
            availableVoices = tts.voices
                ?.filter { voice ->
                    !voice.isNetworkConnectionRequired &&
                        (voice.locale.language == "es" || voice.locale.language == "spa")
                }
                ?.sortedBy { it.name }
                ?: emptyList()
            currentVoice = availableVoices.firstOrNull()
            currentVoice?.let { tts.voice = it }
            Log.d("KairosTtsManager", "TTS ready, voices: ${availableVoices.size}")
        } else {
            Log.e("KairosTtsManager", "TTS init failed with status: $status")
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
        
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kairos_utterance")
        isPlaying = true
        onPlayingChanged?.invoke(true)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("KairosTtsManager", "onStart: $utteranceId")
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

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                Log.d("KairosTtsManager", "onRangeStart: start=$start, end=$end")
                onRangeStart?.invoke(start, end)
            }
        })
    }

    fun stop() {
        tts.stop()
        isPlaying = false
        onPlayingChanged?.invoke(false)
        onRangeStart?.invoke(-1, -1)
    }

    fun setVoice(voice: Voice) {
        currentVoice = voice
        tts.voice = voice
        Log.d("KairosTtsManager", "Voice set to: ${voice.name}")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
    
    fun isReady(): Boolean = isReady
}
