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
import mobi.kairos.android.repository.FavoritesRepository
import mobi.kairos.android.model.FavoriteVerse

class HomeViewModel(
    private val getLastReadVerse: GetLastReadVerseUseCase,
    private val getVerses: GetVersesUseCase,
    private val saveLastReadVerse: SaveLastReadVerseUseCase,
    private val translationBookRepository: TranslationBookRepository,
    private val getOrDownloadChapter: GetOrDownloadChapterUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private val _playingFavoriteId = MutableStateFlow<Long?>(null)
    val playingFavoriteId: StateFlow<Long?> = _playingFavoriteId.asStateFlow()

    private var ttsManagerForFavorites: KairosTtsManager? = null
    private var currentFavoriteText: String = ""

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

    // Estado de favoritos
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    // Estado para la lista de favoritos
    private val _favoriteVerses = MutableStateFlow<List<FavoriteVerse>>(emptyList())
    val favoriteVerses: StateFlow<List<FavoriteVerse>> = _favoriteVerses.asStateFlow()

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

        if (currentSpeakText.length > 2000) {
            currentSpeakText = currentSpeakText.take(2000)
            Log.w("HomeViewModel", "Text truncated to 2000 chars")
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

    fun loadLastReadVerse() {
        viewModelScope.launch {
            getLastReadVerse()
                .onSuccess { verse ->
                    if (verse != null) {
                        Log.d("HomeViewModel", "Loading last read verse: ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}")
                        pendingLastReadVerse = verse

                        if (!isNavigatingFromSplash && !isInitialLoadCompleted) {
                            applyLastReadVerse(verse)
                        } else if (isNavigatingFromSplash) {
                            applyLastReadVerse(verse)
                            isNavigatingFromSplash = false
                        }
                    } else {
                        Log.d("HomeViewModel", "No last read verse found, using default: Genesis 1:1")
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

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesRepository.getAllFavorites().collect { favorites ->
                _favoriteIds.value = favorites.map { "${it.bookId}_${it.chapterNumber}_${it.verseNumber}" }.toSet()
                _favoriteVerses.value = favorites
                Log.d("HomeViewModel", "Loaded ${favorites.size} favorites")
            }
        }
    }

    fun toggleFavorite(verseNumber: Int) {
        viewModelScope.launch {
            val verse = verses.find { it.number == verseNumber }
            if (verse == null) {
                Log.e("HomeViewModel", "Verse $verseNumber not found")
                return@launch
            }

            val verseText = verse.content.joinToString(" ") { it.toText() }
            val key = "${currentBookId}_${currentChapterNumber}_${verseNumber}"
            val isFav = _favoriteIds.value.contains(key)

            if (isFav) {
                favoritesRepository.removeFavorite(currentBookId, currentChapterNumber, verseNumber, currentTranslationId)
                _favoriteIds.update { it - key }
                _favoriteVerses.update { it.filter { fav -> !(fav.bookId == currentBookId && fav.chapterNumber == currentChapterNumber && fav.verseNumber == verseNumber) } }
                Log.d("HomeViewModel", "Removed favorite: $key")
            } else {
                val favorite = FavoriteVerse(
                    bookId = currentBookId,
                    bookName = currentBookName,
                    chapterNumber = currentChapterNumber,
                    verseNumber = verseNumber,
                    verseText = verseText,
                    translationId = currentTranslationId
                )
                favoritesRepository.addFavorite(favorite)
                _favoriteIds.update { it + key }
                _favoriteVerses.update { it + favorite }
                Log.d("HomeViewModel", "Added favorite: $key")
            }
        }
    }

    fun removeFavoriteFromList(favorite: FavoriteVerse) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(favorite)
            _favoriteIds.update { it - "${favorite.bookId}_${favorite.chapterNumber}_${favorite.verseNumber}" }
            _favoriteVerses.update { it.filter { it.id != favorite.id } }
            Log.d("HomeViewModel", "Removed favorite from list: ${favorite.bookName} ${favorite.chapterNumber}:${favorite.verseNumber}")
        }
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
                        loadFavorites()
                    }
                }
                .onFailure {
                    Log.e("HomeViewModel", "Failed to load chapter", it)
                    _uiState.value = HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    private fun updateState() {
        Log.d("HomeViewModel", "Updating state: bookName=$currentBookName bookId=$currentBookId chapter=$currentChapterNumber verse=$lastReadVerseNumber")
        _uiState.value = HomeUiState.Success(
            bookName = currentBookName,
            bookId = currentBookId,
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

    fun navigateToBook(bookId: String, bookName: String, chapterNumber: Int, verseNumber: Int) {
        Log.d("HomeViewModel", "Navigating to specific book: $bookName $chapterNumber:$verseNumber")
        viewModelScope.launch {
            isNavigatingFromSplash = true
            isInitialLoadCompleted = true
            currentBookId = bookId
            currentBookName = bookName
            currentChapterNumber = chapterNumber
            lastReadVerseNumber = verseNumber
            loadChapter()
        }
    }

    fun navigateToLastReadVerse() {
        Log.d("HomeViewModel", "Navigating to last read verse")
        viewModelScope.launch {
            isNavigatingFromSplash = true
            isInitialLoadCompleted = true
            getLastReadVerse()
                .onSuccess { verse ->
                    if (verse != null) {
                        currentTranslationId = verse.translationId
                        currentBookId = verse.bookId
                        currentBookName = verse.bookName
                        currentChapterNumber = verse.chapterNumber
                        lastReadVerseNumber = verse.verseNumber
                    } else {
                        currentBookId = "GEN"
                        currentBookName = "Génesis"
                        currentChapterNumber = 1
                        lastReadVerseNumber = 1
                    }
                    loadChapter()
                }
                .onFailure { error ->
                    Log.e("HomeViewModel", "Failed to load last read verse", error)
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

   /* override fun onCleared() {
        super.onCleared()
        ttsManager?.shutdown()
    }*/

    fun ensureTtsReady(context: Context) {
        if (ttsManager == null) {
            initTts(context)
        } else if (ttsManager?.isReady() == false) {
            viewModelScope.launch {
                var attempts = 0
                while (ttsManager?.isReady() == false && attempts < 10) {
                    delay(500)
                    attempts++
                }
                Log.d("HomeViewModel", "TTS ready after $attempts attempts")
            }
        }
    }

    fun restartTts(context: Context) {
        ttsManager?.shutdown()
        ttsManager = null
        initTts(context)
    }

// Reemplaza las funciones relacionadas con favoritos en HomeViewModel con estas:

    fun initTtsForFavorites(context: Context) {
        if (ttsManagerForFavorites == null) {
            ttsManagerForFavorites = KairosTtsManager(context).also { manager ->
                manager.onPlayingChanged = { playing ->
                    Log.d("HomeViewModel", "TTS for favorites - playing changed: $playing")
                    if (!playing) {
                        // Cuando termina de reproducir, limpiar el estado
                        _playingFavoriteId.value = null
                    }
                }
            }
        }
    }

    fun playFavoriteVerse(favorite: FavoriteVerse) {
        Log.d("HomeViewModel", "playFavoriteVerse called: ${favorite.id}")

        // Si ya se está reproduciendo el mismo, detenerlo
        if (_playingFavoriteId.value == favorite.id) {
            stopFavoritePlayback()
            return
        }

        // Detener cualquier reproducción actual
        stopFavoritePlayback()

        // Asegurar que el TTS esté inicializado
        if (ttsManagerForFavorites == null) {
            Log.e("HomeViewModel", "TTS manager not initialized")
            return
        }

        // Limpiar el estado anterior antes de reproducir
        _playingFavoriteId.value = null

        // Pequeña delay para asegurar limpieza
        viewModelScope.launch {
            delay(50)

            // Verificar si TTS está listo
            val isReady = ttsManagerForFavorites?.isReady() == true
            Log.d("HomeViewModel", "TTS isReady: $isReady")

            if (isReady) {
                _playingFavoriteId.value = favorite.id
                ttsManagerForFavorites?.speak(favorite.verseText)
                Log.d("HomeViewModel", "Playing favorite: ${favorite.bookName} ${favorite.chapterNumber}:${favorite.verseNumber}")
            } else {
                Log.e("HomeViewModel", "TTS not ready, trying to speak anyway")
                // Intentar hablar aunque no esté listo
                _playingFavoriteId.value = favorite.id
                ttsManagerForFavorites?.speak(favorite.verseText)
            }
        }
    }

    fun stopFavoritePlayback() {
        Log.d("HomeViewModel", "stopFavoritePlayback called")
        ttsManagerForFavorites?.stop()
        _playingFavoriteId.value = null
    }

    // Limpiar TTS al cerrar
// Modifica onCleared()
    override fun onCleared() {
        super.onCleared()
        ttsManager?.shutdown()
        ttsManagerForFavorites?.shutdown()
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(
        val bookName: String,
        val bookId: String,
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
