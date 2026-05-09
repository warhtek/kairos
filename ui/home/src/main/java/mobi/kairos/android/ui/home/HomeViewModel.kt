/*
 * © 2026 MOBIWARE. All rights reserved.
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
import mobi.kairos.android.data.AgentClient
import mobi.kairos.android.data.repository.TtsPreferencesRepository
import mobi.kairos.android.model.ChapterVerse
import mobi.kairos.android.repository.TranslationBookRepository
import mobi.kairos.android.usecase.GetLastReadVerseUseCase
import mobi.kairos.android.usecase.GetOrDownloadChapterUseCase
import mobi.kairos.android.usecase.GetVersesUseCase
import mobi.kairos.android.usecase.LastReadVerse
import mobi.kairos.android.usecase.SaveLastReadVerseUseCase
import mobi.kairos.android.repository.FavoritesRepository
import mobi.kairos.android.model.FavoriteVerse
import java.io.File
import org.json.JSONObject
import org.json.JSONArray


class HomeViewModel(
    private val getLastReadVerse: GetLastReadVerseUseCase,
    private val getVerses: GetVersesUseCase,
    private val saveLastReadVerse: SaveLastReadVerseUseCase,
    private val translationBookRepository: TranslationBookRepository,
    private val getOrDownloadChapter: GetOrDownloadChapterUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val ttsPreferencesRepo: TtsPreferencesRepository,
    private val agentClient: AgentClient,
    private val context: android.content.Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private val _playingFavoriteId = MutableStateFlow<Long?>(null)
    val playingFavoriteId: StateFlow<Long?> = _playingFavoriteId.asStateFlow()

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

    private val _favoriteVerses = MutableStateFlow<List<FavoriteVerse>>(emptyList())
    val favoriteVerses: StateFlow<List<FavoriteVerse>> = _favoriteVerses.asStateFlow()

    // Estados del agente IA
    private val _agentResponse = MutableStateFlow<String?>(null)
    val agentResponse: StateFlow<String?> = _agentResponse.asStateFlow()

    private val _isAgentConnected = MutableStateFlow(false)
    val isAgentConnected: StateFlow<Boolean> = _isAgentConnected.asStateFlow()

    private val _isConnectingToAgent = MutableStateFlow(false)
    val isConnectingToAgent: StateFlow<Boolean> = _isConnectingToAgent.asStateFlow()

    // Estados del chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _showChatScreen = MutableStateFlow(false)
    val showChatScreen: StateFlow<Boolean> = _showChatScreen.asStateFlow()

    init {
        val fixedIp = "192.168.0.8"
        val fixedUrl = "http://$fixedIp:8080"
        agentClient.setAgentUrl(fixedUrl)
        _isAgentConnected.value = true
        Log.d("HomeViewModel", "Agent configured with fixed IP: $fixedUrl")
    }

    fun initTts(context: Context) {
        ttsManager = KairosTtsManager.getInstance(context, ttsPreferencesRepo).also { manager ->
            manager.onPlayingChanged = { playing ->
                _ttsState.value = _ttsState.value.copy(isPlaying = playing)
            }
            manager.onRangeStart = { start, end ->
                Log.d("HomeViewModel", "onRangeStart received from TTS: start=$start, end=$end")
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
                } else if (attempts >= 20) {
                    _ttsState.value = _ttsState.value.copy(availableVoices = emptyList())
                }
            }
        }
    }

    fun initTtsForFavorites(context: Context) = initTts(context)

    fun setSpeechRate(rate: Float) {
        ttsManager?.setSpeechRate(rate)
        _ttsState.value = _ttsState.value.copy(currentRate = rate)
    }

    fun setPitch(pitch: Float) {
        ttsManager?.setPitch(pitch)
        _ttsState.value = _ttsState.value.copy(currentPitch = pitch)
    }

    fun isTtsReady(): Boolean = ttsManager?.isReady() == true && ttsManager?.availableVoices?.isNotEmpty() == true

    private var currentSpeakText: String = ""

    fun speakCurrentChapter() {
        Log.d("HomeViewModel", "speakCurrentChapter called - verses size: ${verses.size}")
        if (verses.isEmpty()) {
            Log.w("HomeViewModel", "No verses to speak")
            return
        }
        if (ttsManager == null) {
            Log.e("HomeViewModel", "TTS Manager is null!")
            return
        }

        if (ttsManager?.isReady() != true) {
            Log.e("HomeViewModel", "TTS is not ready!")
            return
        }

        // Construir el texto EXACTAMENTE igual que en la UI
        currentSpeakText = verses.joinToString(" ") { verse ->
            verse.content.joinToString(" ") { it.toText() }
        }

        Log.d("HomeViewModel", "Full text length: ${currentSpeakText.length}")

        ttsManager?.speak(currentSpeakText)
    }

    fun stopSpeaking() = ttsManager?.stop()

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
                        pendingLastReadVerse = verse
                        if (!isNavigatingFromSplash && !isInitialLoadCompleted) applyLastReadVerse(verse)
                        else if (isNavigatingFromSplash) {
                            applyLastReadVerse(verse)
                            isNavigatingFromSplash = false
                        }
                    } else {
                        if (!isInitialLoadCompleted) applyDefaultBook()
                    }
                    isInitialLoadCompleted = true
                    loadChapter()
                }
                .onFailure {
                    if (!isInitialLoadCompleted) applyDefaultBook()
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
    }

    private fun applyDefaultBook() {
        currentBookId = "GEN"
        currentBookName = "Génesis"
        currentChapterNumber = 1
        lastReadVerseNumber = 1
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesRepository.getAllFavorites().collect { favorites ->
                _favoriteIds.value = favorites.map { "${it.bookId}_${it.chapterNumber}_${it.verseNumber}" }.toSet()
                _favoriteVerses.value = favorites
            }
        }
    }

    fun toggleFavorite(verseNumber: Int) {
        viewModelScope.launch {
            val verse = verses.find { it.number == verseNumber } ?: return@launch
            val verseText = verse.content.joinToString(" ") { it.toText() }
            val key = "${currentBookId}_${currentChapterNumber}_${verseNumber}"
            val isFav = _favoriteIds.value.contains(key)

            if (isFav) {
                favoritesRepository.removeFavorite(currentBookId, currentChapterNumber, verseNumber, currentTranslationId)
                _favoriteIds.update { it - key }
                _favoriteVerses.update { it.filter { fav -> !(fav.bookId == currentBookId && fav.chapterNumber == currentChapterNumber && fav.verseNumber == verseNumber) } }
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
            }
        }
    }

    fun removeFavoriteFromList(favorite: FavoriteVerse) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(favorite)
            _favoriteIds.update { it - "${favorite.bookId}_${favorite.chapterNumber}_${favorite.verseNumber}" }
            _favoriteVerses.update { it.filter { it.id != favorite.id } }
        }
    }

    private fun loadChapter() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getOrDownloadChapter(currentTranslationId, currentBookId, currentChapterNumber)
                .onSuccess { chapter ->
                    verses = chapter.chapter.content.filterIsInstance<mobi.kairos.android.model.ChapterVerse>()
                    if (verses.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        updateState()
                        loadFavorites()
                    }
                }
                .onFailure {
                    _uiState.value = HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    private fun updateState() {
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
                    verseText = verses.firstOrNull()?.content?.joinToString(" ") { it.toText() } ?: "",
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

    fun navigateToBook(context: Context, bookId: String, bookName: String, chapterNumber: Int, verseNumber: Int) {
        viewModelScope.launch {
            isNavigatingFromSplash = true
            isInitialLoadCompleted = true
            currentBookId = bookId
            currentBookName = bookName
            currentChapterNumber = chapterNumber
            lastReadVerseNumber = verseNumber

            ensureTtsReadyForNavigation(context)
            loadChapter()
        }
    }

    fun navigateToLastReadVerse() {
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
                .onFailure {
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
                    verseText = verses.find { it.number == verseNumber }?.content?.joinToString(" ") { it.toText() } ?: "",
                )
            )
        }
    }

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
            }
        }
    }

    fun restartTts(context: Context) {
        ttsManager?.shutdown()
        ttsManager = null
        initTts(context)
    }

    fun playFavoriteVerse(favorite: FavoriteVerse) {
        if (_playingFavoriteId.value == favorite.id) {
            stopFavoritePlayback()
            return
        }
        stopFavoritePlayback()
        if (ttsManager == null) return

        _playingFavoriteId.value = null
        viewModelScope.launch {
            delay(50)
            if (ttsManager?.isReady() == true) {
                _playingFavoriteId.value = favorite.id
                ttsManager?.speak(favorite.verseText)
            }
        }
    }

    fun stopFavoritePlayback() {
        ttsManager?.stop()
        _playingFavoriteId.value = null
    }

    fun connectToAgent() {
        _isConnectingToAgent.value = false
    }

    // ============ FUNCIONES DEL CHAT ============

    fun toggleChatScreen() {
        _showChatScreen.value = !_showChatScreen.value
    }

    fun closeChatScreen() {
        _showChatScreen.value = false
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
    }

    fun sendChatMessage(message: String) {
        if (message.isBlank() || _isChatLoading.value) return

        val userMessage = ChatMessage(text = message, isUser = true)
        _chatMessages.update { it + userMessage }

        val loadingMessage = ChatMessage(text = "", isUser = false, isLoading = true)
        _chatMessages.update { it + loadingMessage }
        _isChatLoading.value = true

        viewModelScope.launch {
            val localResults = searchLocalBible(message)

            val enhancedMessage = if (localResults.isNotEmpty()) {
                "$localResults\n\nBasado SOLO en los versículos anteriores, responde esta pregunta: $message"
            } else {
                "No se encontraron versículos relacionados. Pregunta: $message"
            }

            val result = agentClient.chat(enhancedMessage, "bible")

            _chatMessages.update { messages -> messages.filterNot { it.isLoading } }

            if (result.isSuccess) {
                val agentMessage = ChatMessage(text = result.getOrNull() ?: "No se pudo obtener respuesta", isUser = false)
                _chatMessages.update { it + agentMessage }
            } else {
                val errorMessage = ChatMessage(text = "❌ Error: ${result.exceptionOrNull()?.message}", isUser = false)
                _chatMessages.update { it + errorMessage }
            }
            _isChatLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager?.shutdown()
    }

    fun searchLocalBible(query: String): String {
        return try {
            val translationId = currentTranslationId
            val bibleFile = File(context.filesDir, "translations/$translationId.json")

            if (!bibleFile.exists()) return ""

            val jsonContent = bibleFile.readText()
            val jsonObject = JSONObject(jsonContent)
            val books = jsonObject.getJSONArray("books")

            val results = mutableListOf<String>()

            for (i in 0 until books.length()) {
                val book = books.getJSONObject(i)
                val bookName = book.getString("name")
                val chapters = book.getJSONArray("chapters")

                for (j in 0 until chapters.length()) {
                    val chapter = chapters.getJSONObject(j)
                    val chapterNum = chapter.getJSONObject("chapter").getInt("number")
                    val contentArray = chapter.getJSONObject("chapter").getJSONArray("content")

                    for (k in 0 until contentArray.length()) {
                        val element = contentArray.getJSONObject(k)
                        if (element.has("type") && element.getString("type") == "verse") {
                            val verseNum = element.getInt("number")
                            val content = element.getJSONArray("content")
                            val verseText = (0 until content.length()).joinToString(" ") { idx ->
                                val item = content.get(idx)
                                when (item) {
                                    is String -> item
                                    else -> item.toString()
                                }
                            }

                            if (verseText.contains(query, ignoreCase = true)) {
                                results.add("$bookName $chapterNum:$verseNum - $verseText")
                                if (results.size >= 5) break
                            }
                        }
                    }
                    if (results.size >= 5) break
                }
                if (results.size >= 5) break
            }

            if (results.isNotEmpty()) {
                "Versículos encontrados:\n" + results.joinToString("\n")
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error searching local Bible", e)
            ""
        }
    }

    fun ensureTtsReadyForNavigation(context: Context) {
        if (ttsManager == null) {
            initTts(context)
        } else if (ttsManager?.isReady() == false) {
            viewModelScope.launch {
                var attempts = 0
                while (ttsManager?.isReady() == false && attempts < 20) {
                    delay(500)
                    attempts++
                }
                if (ttsManager?.isReady() == true) {
                    Log.d("HomeViewModel", "TTS ready after waiting")
                } else {
                    Log.w("HomeViewModel", "TTS not ready after waiting, reinitializing")
                    restartTts(context)
                }
            }
        }
    }

    fun refreshTtsLanguage() {
        ttsManager?.refreshLanguage()
        ttsManager?.let {
            _ttsState.value = _ttsState.value.copy(
                availableVoices = it.availableVoices,
                currentVoice = it.currentVoice
            )
        }
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
    val currentRate: Float = 0.9f,
    val currentPitch: Float = 1.0f,
)
