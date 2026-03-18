/*
 * © 2026 MOBIWARE. All rights reserved.
 *
 * This software and its source code are the exclusive property of MOBIWARE.
 * Any unauthorized use, reproduction, distribution, modification, or disclosure
 * of this software, whether in whole or in part, is strictly prohibited.
 *
 * Violations may result in severe civil and criminal penalties under applicable
 * copyright, intellectual property, and trade secret laws.
 */
package mobi.kairos.android.ui.home

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
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

    override fun onInit(status: Int) {
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
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kairos_utterance")
        isPlaying = true
        onPlayingChanged?.invoke(true)
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                isPlaying = false
                onPlayingChanged?.invoke(false)
            }
            override fun onError(utteranceId: String?) {
                isPlaying = false
                onPlayingChanged?.invoke(false)
            }
        })
    }

    fun stop() {
        tts.stop()
        isPlaying = false
        onPlayingChanged?.invoke(false)
    }

    fun setVoice(voice: Voice) {
        currentVoice = voice
        tts.voice = voice
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
