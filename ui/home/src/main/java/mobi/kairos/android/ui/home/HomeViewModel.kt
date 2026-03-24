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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobi.kairos.android.model.ChapterVerse
import mobi.kairos.android.repository.TranslationBookRepository
import mobi.kairos.android.usecase.GetLastReadVerseUseCase
import mobi.kairos.android.usecase.GetOrDownloadChapterUseCase
import mobi.kairos.android.usecase.GetVersesUseCase
import mobi.kairos.android.usecase.LastReadVerse
import mobi.kairos.android.usecase.SaveLastReadVerseUseCase

class HomeViewModel(
    private val getLastReadVerse: GetLastReadVerseUseCase,
    private val getVerses: GetVersesUseCase,
    private val saveLastReadVerse: SaveLastReadVerseUseCase,
    private val translationBookRepository: TranslationBookRepository,
    private val getOrDownloadChapter: GetOrDownloadChapterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private var ttsManager: KairosTtsManager? = null
    private var verses: List<ChapterVerse> = emptyList()
    private var currentTranslationId: String = "spa_bes"
    private var currentBookId: String = "GEN"
    private var currentBookName: String = "Génesis"
    private var currentChapterNumber: Int = 1
    private var lastReadVerseNumber: Int = 1

    init {
        loadLastReadVerse()
    }

    fun initTts(context: Context) {
        ttsManager = KairosTtsManager(context).also { manager ->
            manager.onPlayingChanged = { playing ->
                _ttsState.value = _ttsState.value.copy(isPlaying = playing)
            }
            manager.onRangeStart = { start, end ->
                _ttsState.value = _ttsState.value.copy(
                    highlightStart = start,
                    highlightEnd = end,
                )
            }
        }
        viewModelScope.launch {
            delay(1000)
            _ttsState.value = _ttsState.value.copy(
                availableVoices = ttsManager?.availableVoices ?: emptyList(),
                currentVoice = ttsManager?.currentVoice,
            )
        }
    }

    private var currentSpeakText: String = ""

    fun speakCurrentChapter() {
        currentSpeakText = verses.joinToString(" ") { verse ->
            verse.content.joinToString(" ") { it.toText() }
        }
        ttsManager?.speak(currentSpeakText)
    }

    fun stopSpeaking() {
        ttsManager?.stop()
    }

    fun setVoice(voice: Voice) {
        ttsManager?.setVoice(voice)
        _ttsState.value = _ttsState.value.copy(currentVoice = voice)
    }

    fun changeTranslation(translationId: String) {
        stopSpeaking()
        currentTranslationId = translationId
        lastReadVerseNumber = 1
        loadChapter()
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
                        lastReadVerseNumber = verse.verseNumber
                    }
                    loadChapter()
                }
                .onFailure {
                    _uiState.value = HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    private fun loadChapter() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getOrDownloadChapter(currentTranslationId, currentBookId, currentChapterNumber)
                .onSuccess { chapter ->
                    verses = chapter.chapter.content
                        .filterIsInstance<mobi.kairos.android.model.ChapterVerse>()
                    if (verses.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        updateState()
                    }
                }
                .onFailure {
                    _uiState.value = HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    private fun updateState() {
        Log.d("HomeViewModel", "bookName=$currentBookName bookId=$currentBookId chapter=$currentChapterNumber")
        _uiState.value = HomeUiState.Success(
            bookName = currentBookName,
            chapterNumber = currentChapterNumber,
            translationId = currentTranslationId,
            verses = verses,
            hasPrevious = currentChapterNumber > 1,
            hasNext = true,
            scrollToVerse = lastReadVerseNumber,
        )
        viewModelScope.launch {
            saveLastReadVerse(
                LastReadVerse(
                    translationId = currentTranslationId,
                    bookId = currentBookId,
                    bookName = currentBookName,
                    chapterNumber = currentChapterNumber,
                    verseNumber = lastReadVerseNumber,
                    verseText = verses.firstOrNull()?.content
                        ?.joinToString(" ") { it.toText() } ?: "",
                )
            )
        }
    }

    fun navigateNextChapter() {
        stopSpeaking()
        viewModelScope.launch {
            val currentBook = translationBookRepository.getBookById(currentBookId)
            if (currentBook != null && currentChapterNumber < currentBook.numberOfChapters) {
                currentChapterNumber++
                lastReadVerseNumber = 1
                loadChapter()
            } else {
                val nextBook = translationBookRepository.getNextBook(currentBookId)
                if (nextBook != null) {
                    currentBookId = nextBook.id
                    currentBookName = nextBook.name
                    currentChapterNumber = 1
                    lastReadVerseNumber = 1
                    loadChapter()
                }
            }
        }
    }

    fun navigatePreviousChapter() {
        stopSpeaking()
        viewModelScope.launch {
            if (currentChapterNumber > 1) {
                currentChapterNumber--
                lastReadVerseNumber = 1
                loadChapter()
            } else {
                val previousBook = translationBookRepository.getPreviousBook(currentBookId)
                if (previousBook != null) {
                    currentBookId = previousBook.id
                    currentBookName = previousBook.name
                    currentChapterNumber = previousBook.numberOfChapters
                    lastReadVerseNumber = 1
                    loadChapter()
                }
            }
        }
    }

    fun navigateToBook(
        bookId: String,
        bookName: String,
        chapterNumber: Int = 1,
        verseNumber: Int = 1,
    ) {
        stopSpeaking()
        currentBookId = bookId
        currentBookName = bookName
        currentChapterNumber = chapterNumber
        lastReadVerseNumber = verseNumber
        loadChapter()
    }

    fun onVerseVisible(verseNumber: Int) {
        lastReadVerseNumber = verseNumber
        viewModelScope.launch {
            saveLastReadVerse(
                LastReadVerse(
                    translationId = currentTranslationId,
                    bookId = currentBookId,
                    bookName = currentBookName,
                    chapterNumber = currentChapterNumber,
                    verseNumber = verseNumber,
                    verseText = verses.find { it.number == verseNumber }
                        ?.content?.joinToString(" ") { it.toText() } ?: "",
                )
            )
        }
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
        val bookName: String,
        val chapterNumber: Int,
        val translationId: String,
        val verses: List<ChapterVerse>,
        val hasPrevious: Boolean,
        val hasNext: Boolean,
        val scrollToVerse: Int = 1,
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

data class TtsState(
    val isPlaying: Boolean = false,
    val availableVoices: List<android.speech.tts.Voice> = emptyList(),
    val currentVoice: android.speech.tts.Voice? = null,
    val highlightStart: Int = -1,
    val highlightEnd: Int = -1,
)
