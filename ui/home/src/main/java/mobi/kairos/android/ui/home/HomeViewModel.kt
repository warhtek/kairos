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
import android.speech.tts.Voice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobi.kairos.android.model.ChapterVerse
import mobi.kairos.android.usecase.GetLastReadVerseUseCase
import mobi.kairos.android.usecase.GetVersesUseCase
import mobi.kairos.android.usecase.LastReadVerse
import mobi.kairos.android.usecase.SaveLastReadVerseUseCase

class HomeViewModel(
    private val getLastReadVerse: GetLastReadVerseUseCase,
    private val getVerses: GetVersesUseCase,
    private val saveLastReadVerse: SaveLastReadVerseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private var ttsManager: KairosTtsManager? = null
    private var verses: List<ChapterVerse> = emptyList()
    private var currentIndex: Int = 0
    private var currentTranslationId: String = "spa_bes"
    private var currentBookId: String = "GEN"
    private var currentBookName: String = "Génesis"
    private var currentChapterNumber: Int = 1

    init {
        loadLastReadVerse()
    }

    fun initTts(context: Context) {
        ttsManager = KairosTtsManager(context).also { manager ->
            manager.onPlayingChanged = { playing ->
                _ttsState.value = _ttsState.value.copy(isPlaying = playing)
            }
        }
        // dar tiempo al TTS para inicializarse
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _ttsState.value = _ttsState.value.copy(
                availableVoices = ttsManager?.availableVoices ?: emptyList(),
                currentVoice = ttsManager?.currentVoice,
            )
        }
    }

    fun speakCurrentVerse() {
        val state = _uiState.value
        if (state is HomeUiState.Success) {
            ttsManager?.speak(state.verse.verseText)
        }
    }

    fun stopSpeaking() {
        ttsManager?.stop()
    }

    fun setVoice(voice: Voice) {
        ttsManager?.setVoice(voice)
        _ttsState.value = _ttsState.value.copy(currentVoice = voice)
    }

    private fun loadLastReadVerse() {
        viewModelScope.launch {
            getLastReadVerse()
                .onSuccess { verse ->
                    if (verse != null) {
                        currentTranslationId = verse.translationId
                        currentBookId = verse.bookId
                        currentBookName = verse.bookName
                        currentChapterNumber = verse.chapterNumber
                        loadVerses(verse.verseNumber - 1)
                    } else {
                        loadVerses(0)
                    }
                }
                .onFailure {
                    _uiState.value = HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    private fun loadVerses(startIndex: Int = 0) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getVerses(currentTranslationId, currentBookId, currentChapterNumber)
                .onSuccess { result ->
                    verses = result
                    if (verses.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        currentIndex = startIndex.coerceIn(0, verses.size - 1)
                        updateCurrentVerse()
                    }
                }
                .onFailure {
                    _uiState.value = HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    private fun updateCurrentVerse() {
        val verse = verses[currentIndex]
        Log.d("HomeViewModel", "bookName=$currentBookName bookId=$currentBookId chapter=$currentChapterNumber verse=${verse.number}")
        val lastRead = LastReadVerse(
            translationId = currentTranslationId,
            bookId = currentBookId,
            bookName = currentBookName,
            chapterNumber = currentChapterNumber,
            verseNumber = verse.number,
            verseText = verse.content.joinToString(" ") { it.toText() },
        )
        _uiState.value = HomeUiState.Success(
            verse = lastRead,
            hasPrevious = currentIndex > 0 || currentChapterNumber > 1,
            hasNext = true,
        )
        viewModelScope.launch {
            saveLastReadVerse(lastRead)
        }
    }

    fun navigateNext() {
        Log.d("HomeViewModel", "navigateNext: index=$currentIndex verses=${verses.size} chapter=$currentChapterNumber")
        stopSpeaking()
        if (currentIndex < verses.size - 1) {
            currentIndex++
            updateCurrentVerse()
        } else {
            Log.d("HomeViewModel", "navigateNext: moving to chapter ${currentChapterNumber + 1}")
            currentChapterNumber++
            currentIndex = 0
            loadVerses(0)
        }
    }

    fun navigatePrevious() {
        stopSpeaking()
        if (currentIndex > 0) {
            currentIndex--
            updateCurrentVerse()
        } else if (currentChapterNumber > 1) {
            currentChapterNumber--
            currentIndex = Int.MAX_VALUE
            loadVerses(Int.MAX_VALUE)
        }
    }

    fun navigateToBook(bookId: String, bookName: String) {
        stopSpeaking()
        currentBookId = bookId
        currentBookName = bookName
        currentChapterNumber = 1
        currentIndex = 0
        loadVerses(0)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager?.shutdown()
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(
        val verse: LastReadVerse,
        val hasPrevious: Boolean,
        val hasNext: Boolean,
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

data class TtsState(
    val isPlaying: Boolean = false,
    val availableVoices: List<android.speech.tts.Voice> = emptyList(),
    val currentVoice: android.speech.tts.Voice? = null,
    val showVoiceSelector: Boolean = false,
)
