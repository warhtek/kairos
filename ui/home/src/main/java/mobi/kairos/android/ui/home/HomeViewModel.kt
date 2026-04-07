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
import kotlinx.coroutines.flow.update
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
    private var isNavigatingFromSplash: Boolean = false
    private var isInitialLoadCompleted: Boolean = false
    private var pendingLastReadVerse: LastReadVerse? = null

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

        // Esperar activamente a que las voces estén disponibles
        viewModelScope.launch {
            var attempts = 0
            var voicesLoaded = false

            while (!voicesLoaded && attempts < 20) {
                delay(500)
                attempts++

                val voices = ttsManager?.availableVoices
                if (voices != null && voices.isNotEmpty()) {
                    voicesLoaded = true
                    _ttsState.value = _ttsState.value.copy(
                        availableVoices = voices,
                        currentVoice = ttsManager?.currentVoice
                    )
                    Log.d("HomeViewModel", "Voices loaded after $attempts attempts: ${voices.size} voices")
                } else if (attempts >= 20) {
                    Log.w("HomeViewModel", "Failed to load voices after $attempts attempts")
                    _ttsState.value = _ttsState.value.copy(availableVoices = emptyList())
                }
            }
        }
    }

    fun isTtsReady(): Boolean {
        return ttsManager?.isReady() == true && ttsManager?.availableVoices?.isNotEmpty() == true
    }

    private var currentSpeakText: String = ""

    fun speakCurrentChapter() {
        if (verses.isEmpty()) {
            Log.w("HomeViewModel", "No verses to speak")
            return
        }

        currentSpeakText = verses.joinToString(" ") { verse ->
            verse.content.joinToString(" ") { it.toText() }
        }

        Log.d("HomeViewModel", "Speaking text length: ${currentSpeakText.length}")
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

    // Load last read verse - used when navigating to Bible from splash
    fun loadLastReadVerse() {
        viewModelScope.launch {
            getLastReadVerse()
                .onSuccess { verse ->
                    if (verse != null) {
                        Log.d("HomeViewModel", "Loading last read verse: ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}")
                        pendingLastReadVerse = verse

                        if (!isNavigatingFromSplash && !isInitialLoadCompleted) {
                            // Initial app launch - load last read verse
                            applyLastReadVerse(verse)
                        } else if (isNavigatingFromSplash) {
                            // Navigating from Bible button - load last read verse
                            applyLastReadVerse(verse)
                            isNavigatingFromSplash = false
                        }
                    } else {
                        Log.d("HomeViewModel", "No last read verse found, using default: Genesis 1:1")
                        // No saved verse, use default
                        if (!isInitialLoadCompleted) {
                            applyDefaultBook()
                        }
                    }
                    isInitialLoadCompleted = true
                    loadChapter()
                }
                .onFailure {
                    Log.e("HomeViewModel", "Failed to load last read verse", it)
                    if (!isInitialLoadCompleted) {
                        applyDefaultBook()
                    }
                    isInitialLoadCompleted = true
                    loadChapter()
                }
        }
    }

    private fun applyLastReadVerse(verse: LastReadVerse) {
        currentTranslationId = verse.translationId
        currentBookId = verse.bookId
        currentBookName = verse.bookName
        currentChapterNumber = verse.chapterNumber
        lastReadVerseNumber = verse.verseNumber
        Log.d("HomeViewModel", "Applied last read verse: $currentBookName $currentChapterNumber:$lastReadVerseNumber")
    }

    private fun applyDefaultBook() {
        currentBookId = "GEN"
        currentBookName = "Génesis"
        currentChapterNumber = 1
        lastReadVerseNumber = 1
        Log.d("HomeViewModel", "Applied default book: Genesis 1:1")
    }

    private fun loadChapter() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            Log.d("HomeViewModel", "Loading chapter: translation=$currentTranslationId, book=$currentBookId, chapter=$currentChapterNumber")
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
                    Log.e("HomeViewModel", "Failed to load chapter", it)
                    _uiState.value = HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    private fun updateState() {
        Log.d("HomeViewModel", "Updating state: bookName=$currentBookName bookId=$currentBookId chapter=$currentChapterNumber verse=$lastReadVerseNumber isNavigatingFromSplash=$isNavigatingFromSplash")
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
        isNavigatingFromSplash = false
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
        isNavigatingFromSplash = false
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

    // Navigate to a specific book, chapter, and verse from SplashScreen (daily verse click)
    fun navigateToBook(bookId: String, bookName: String, chapterNumber: Int, verseNumber: Int) {
        Log.d("HomeViewModel", "Navigating to specific book: $bookName $chapterNumber:$verseNumber")
        stopSpeaking()
        viewModelScope.launch {
            // Set flag to prevent loading last read verse
            isNavigatingFromSplash = true
            isInitialLoadCompleted = true

            // Update current state with the new book, chapter, and verse
            currentBookId = bookId
            currentBookName = bookName
            currentChapterNumber = chapterNumber
            lastReadVerseNumber = verseNumber

            // Load the chapter
            loadChapter()
        }
    }

    // Navigate to last read verse (called when pressing Bible button)
    fun navigateToLastReadVerse() {
        Log.d("HomeViewModel", "Navigating to last read verse")
        stopSpeaking()
        viewModelScope.launch {
            // Set flag to indicate we're navigating from splash
            isNavigatingFromSplash = true
            isInitialLoadCompleted = true

            // Load the last read verse
            getLastReadVerse()
                .onSuccess { verse ->
                    if (verse != null) {
                        Log.d("HomeViewModel", "Found last read verse: ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}")
                        currentTranslationId = verse.translationId
                        currentBookId = verse.bookId
                        currentBookName = verse.bookName
                        currentChapterNumber = verse.chapterNumber
                        lastReadVerseNumber = verse.verseNumber
                    } else {
                        Log.d("HomeViewModel", "No last read verse found, using default: Genesis 1:1")
                        currentBookId = "GEN"
                        currentBookName = "Génesis"
                        currentChapterNumber = 1
                        lastReadVerseNumber = 1
                    }
                    loadChapter()
                }
                .onFailure { error ->
                    Log.e("HomeViewModel", "Failed to load last read verse", error)
                    // Fallback to default
                    currentBookId = "GEN"
                    currentBookName = "Génesis"
                    currentChapterNumber = 1
                    lastReadVerseNumber = 1
                    loadChapter()
                }
        }
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
