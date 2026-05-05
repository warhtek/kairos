/*
 * © 2026 MOBIWARE. All rights reserved.
 */
package mobi.kairos.android.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mobi.kairos.android.model.ChapterVerse
import mobi.kairos.android.model.FavoriteVerse
import mobi.kairos.android.ui.books.BooksViewModel
import mobi.kairos.android.ui.books.BooksUiState
import mobi.kairos.android.ui.common.Clickable
import mobi.kairos.android.ui.search.SearchViewModel
import mobi.kairos.android.ui.search.SearchUiState
import mobi.kairos.android.ui.splash.SplashViewModel
import mobi.kairos.android.ui.splash.SplashUiState
import mobi.kairos.android.ui.translations.TranslationsViewModel
import mobi.kairos.android.ui.translations.TranslationsUiState
import mobi.kairos.android.ui.translations.TranslationItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedBookId: String? = null,
    selectedBookName: String? = null,
    selectedChapterNumber: Int = 1,
    selectedVerseNumber: Int = 1,
    selectedTranslationId: String? = null,
    onBookSelected: () -> Unit = {},
    onTranslationChanged: () -> Unit = {},
    onNavigateToSplash: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    booksViewModel: BooksViewModel = koinViewModel(),
    translationsViewModel: TranslationsViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
    splashViewModel: SplashViewModel = koinViewModel(),
) {
    Log.d("HomeScreen", "Rendering with params - bookId: $selectedBookId, bookName: $selectedBookName")

    var initialNavigationHandled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(selectedBookId, selectedBookName, selectedChapterNumber, selectedVerseNumber) {
        if (!initialNavigationHandled) {
            if (selectedBookId != null && selectedBookName != null) {
                viewModel.navigateToBook(
                    context = context,
                    bookId = selectedBookId,
                    bookName = selectedBookName,
                    chapterNumber = selectedChapterNumber,
                    verseNumber = selectedVerseNumber
                )
                onBookSelected()
            } else {
                viewModel.navigateToLastReadVerse()
            }
            initialNavigationHandled = true
        }
    }

    LaunchedEffect(selectedTranslationId) {
        if (selectedTranslationId != null) {
            viewModel.changeTranslation(selectedTranslationId)
            onTranslationChanged()
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        translationsViewModel.setContext(context)
        viewModel.initTts(context)
        viewModel.connectToAgent()
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.ensureTtsReady(context)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val booksUiState by booksViewModel.uiState.collectAsStateWithLifecycle()
    val translationsUiState by translationsViewModel.uiState.collectAsStateWithLifecycle()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by searchViewModel.query.collectAsStateWithLifecycle()

    val booksSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val translationsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val voiceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var showBooksSheet by remember { mutableStateOf(false) }
    var showTranslationsSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }
    var expandedBookId by remember { mutableStateOf<String?>(null) }
    var sortAlphabetically by remember { mutableStateOf(false) }
    var showDailyVerseSheet by remember { mutableStateOf(false) }
    var showFavoritesSheet by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedTranslationToDownload by remember { mutableStateOf<TranslationItem?>(null) }

    var showDeleteFavoriteDialog by remember { mutableStateOf(false) }
    var favoriteToDelete by remember { mutableStateOf<FavoriteVerse?>(null) }

    if (showConfirmDialog && selectedTranslationToDownload != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.translations_confirm_download_title, selectedTranslationToDownload?.name ?: "")) },
            text = { Text(stringResource(R.string.translations_confirm_download_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        selectedTranslationToDownload?.let { translation ->
                            translationsViewModel.downloadTranslation(translation) { downloadedId ->
                                viewModel.changeTranslation(downloadedId)
                                onTranslationChanged()
                                scope.launch { translationsSheetState.hide() }
                                showTranslationsSheet = false
                            }
                        }
                    }
                ) { Text(stringResource(R.string.translations_download)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showDeleteFavoriteDialog && favoriteToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteFavoriteDialog = false
                favoriteToDelete = null
            },
            title = { Text(stringResource(R.string.favorites_remove_confirm_title)) },
            text = {
                Text(stringResource(R.string.favorites_remove_confirm_message) + "\n\n${favoriteToDelete?.bookName} ${favoriteToDelete?.chapterNumber}:${favoriteToDelete?.verseNumber}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        favoriteToDelete?.let { viewModel.removeFavoriteFromList(it) }
                        showDeleteFavoriteDialog = false
                        favoriteToDelete = null
                    }
                ) { Text(stringResource(R.string.favorites_remove), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteFavoriteDialog = false
                        favoriteToDelete = null
                    }
                ) { Text(stringResource(R.string.favorites_cancel)) }
            }
        )
    }

    fun getTranslationSize(translationId: String?): String = when (translationId) {
        "spa_bes" -> "5.2"
        "BSB" -> "6.8"
        "AAB" -> "4.5"
        "ARBNAV" -> "8.2"
        "HINIRV" -> "7.1"
        else -> "3.0"
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        val currentUiState = uiState
                        when (currentUiState) {
                            is HomeUiState.Success -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.navigatePreviousChapter() },
                                            enabled = currentUiState.hasPrevious,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.SkipPrevious,
                                                    contentDescription = stringResource(R.string.home_previous),
                                                    modifier = Modifier.size(24.dp),
                                                    tint = if (currentUiState.hasPrevious) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                )
                                                Text(
                                                    text = "${currentUiState.chapterNumber - 1}".takeIf { currentUiState.hasPrevious } ?: "",
                                                    fontSize = 14.sp,
                                                )
                                            }
                                        }

                                        FilledTonalIconButton(
                                            onClick = {
                                                if (ttsState.isPlaying) viewModel.stopSpeaking()
                                                else {
                                                    viewModel.ensureTtsReady(context)
                                                    viewModel.speakCurrentChapter()
                                                }
                                            },
                                            modifier = Modifier.size(40.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (ttsState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = if (ttsState.isPlaying) stringResource(R.string.home_stop) else stringResource(R.string.home_play),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        TextButton(
                                            onClick = { viewModel.navigateNextChapter() },
                                            enabled = currentUiState.hasNext,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${currentUiState.chapterNumber + 1}",
                                                    fontSize = 14.sp,
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.SkipNext,
                                                    contentDescription = stringResource(R.string.home_next),
                                                    modifier = Modifier.size(24.dp),
                                                    if (currentUiState.hasNext) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(0.dp))
                                }
                            }
                            else -> Text(stringResource(R.string.app_name))
                        }
                    },
                    actions = {
                        val currentUiState = uiState
                        if (currentUiState is HomeUiState.Success) {
                            IconButton(onClick = { showFavoritesSheet = true }) {
                                Icon(Icons.Filled.Favorite, contentDescription = stringResource(R.string.home_favorites))
                            }
                            Clickable(onClick = { showTranslationsSheet = true }) {
                                Text(
                                    text = currentUiState.translationId.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(4.dp),
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                            IconButton(onClick = { showSearchSheet = true }) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.home_search))
                            }
                        }
                    },
                )
                val currentUiStateForBooks = uiState
                if (currentUiStateForBooks is HomeUiState.Success) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        TextButton(
                            onClick = { showBooksSheet = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${currentUiStateForBooks.bookName} ${currentUiStateForBooks.chapterNumber}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.advertisement), style = MaterialTheme.typography.labelSmall)
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = { showSearchSheet = true }) { Icon(Icons.Default.Search, null) }
                        Text(stringResource(R.string.home_nav_search), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = { showBooksSheet = true }) { Icon(Icons.Default.MenuBook, null) }
                        Text(stringResource(R.string.home_nav_books), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = { showDailyVerseSheet = true }) { Icon(Icons.Default.Today, null) }
                        Text(stringResource(R.string.home_nav_today), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = { showTranslationsSheet = true }) { Icon(Icons.Default.Translate, null) }
                        Text(stringResource(R.string.home_nav_version), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = { showVoiceSheet = true }) { Icon(Icons.Default.VolumeUp, null) }
                        Text(stringResource(R.string.home_nav_voice), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val currentUiState = uiState
            when (currentUiState) {
                is HomeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is HomeUiState.Empty -> Text(
                    stringResource(R.string.no_verses_available),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                is HomeUiState.Success -> ChapterContent(
                    verses = currentUiState.verses,
                    scrollToVerse = currentUiState.scrollToVerse,
                    onVerseVisible = { viewModel.onVerseVisible(it) },
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    favoriteIds = favoriteIds,
                    currentBookId = currentUiState.bookId,
                    currentChapterNumber = currentUiState.chapterNumber,
                    highlightStart = ttsState.highlightStart,
                    highlightEnd = ttsState.highlightEnd,
                )
                is HomeUiState.Error -> Text(
                    "${stringResource(R.string.error)}: ${currentUiState.message}",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            }

            val showChat by viewModel.showChatScreen.collectAsStateWithLifecycle()

            if (showChat) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ChatScreen(
                        viewModel = viewModel,
                        onClose = { viewModel.toggleChatScreen() }
                    )
                }
            }
            if (!showChat) {
                FloatingActionButton(
                    onClick = { viewModel.toggleChatScreen() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = stringResource(R.string.chat_ai))
                }
            }
        }
    }

    // Daily verse bottom sheet
    if (showDailyVerseSheet) {
        val dailyUiState by splashViewModel.uiState.collectAsStateWithLifecycle()
        ModalBottomSheet(
            onDismissRequest = { showDailyVerseSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.daily_verse_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (val state = dailyUiState) {
                    is SplashUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is SplashUiState.Success -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${state.verse.bookName} ${state.verse.chapterNumber}:${state.verse.verseNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = state.verse.verseText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    lineHeight = 26.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    onNavigateToSplash()
                                    showDailyVerseSheet = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.daily_verse_go_to_bible))
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.daily_verse_not_available),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Books bottom sheet
    if (showBooksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBooksSheet = false },
            sheetState = booksSheetState,
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { showBooksSheet = false }) { Text(stringResource(R.string.home_books_cancel)) }
                    Text(stringResource(R.string.home_books_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { sortAlphabetically = !sortAlphabetically }) {
                        Text(if (sortAlphabetically) stringResource(R.string.home_books_traditional) else stringResource(R.string.home_books_alphabetical))
                    }
                }
                HorizontalDivider()
                when (val state = booksUiState) {
                    is BooksUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is BooksUiState.Success -> {
                        val books = if (sortAlphabetically) state.books.sortedBy { it.name }
                        else state.books.sortedBy { it.order }
                        LazyColumn {
                            items(books) { book ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(book.name.uppercase(), modifier = Modifier.weight(1f))
                                        IconButton(onClick = { expandedBookId = if (expandedBookId == book.id) null else book.id }) {
                                            Icon(
                                                if (expandedBookId == book.id) Icons.Default.ArrowCircleUp else Icons.Default.ArrowCircleDown,
                                                null,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                    if (expandedBookId == book.id) {
                                        val chapters = (1..book.numberOfChapters).toList()
                                        val rows = chapters.chunked(5)
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            rows.forEach { rowChapters ->
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    rowChapters.forEach { chapter ->
                                                        Clickable(
                                                            modifier = Modifier
                                                                .size(52.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                                            onClick = {
                                                                viewModel.navigateToBook(
                                                                    context = context,
                                                                    bookId = book.id,
                                                                    bookName = book.name,
                                                                    chapterNumber = chapter,
                                                                    verseNumber = 1
                                                                )
                                                                scope.launch { booksSheetState.hide() }
                                                                showBooksSheet = false
                                                            }
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text("$chapter", style = MaterialTheme.typography.bodyMedium)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    // Translations bottom sheet
    if (showTranslationsSheet) {
        val uiStateValue by translationsViewModel.uiState.collectAsStateWithLifecycle()
        val downloadingId by translationsViewModel.downloadingTranslation.collectAsStateWithLifecycle()
        val selectedId by translationsViewModel.selectedTranslationId.collectAsStateWithLifecycle()

        var translationToDelete by remember { mutableStateOf<TranslationItem?>(null) }
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { showTranslationsSheet = false },
            sheetState = translationsSheetState,
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { showTranslationsSheet = false }) { Text(stringResource(R.string.translations_cancel)) }
                    Text(stringResource(R.string.translations_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.size(64.dp))
                }
                HorizontalDivider()

                when (val state = uiStateValue) {
                    is TranslationsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is TranslationsUiState.Success -> {
                        val translations = state.translations
                        if (translations.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.translations_no_versions))
                            }
                        } else {
                            LazyColumn {
                                items(translations) { translation ->
                                    val isSelected = selectedId == translation.id
                                    val isDownloading = downloadingId == translation.id

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = translation.isDownloaded && !isDownloading) {
                                                    if (translation.isDownloaded) {
                                                        translationsViewModel.selectTranslation(translation.id)
                                                        viewModel.changeTranslation(translation.id)
                                                        onTranslationChanged()
                                                        scope.launch { translationsSheetState.hide() }
                                                        showTranslationsSheet = false
                                                    }
                                                },
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                        else MaterialTheme.colorScheme.secondaryContainer
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    translation.shortName.take(4).uppercase(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                                )
                                            }
                                            Column {
                                                Text(translation.languageName, style = MaterialTheme.typography.labelSmall)
                                                Text(translation.name, style = MaterialTheme.typography.bodyLarge)
                                            }
                                        }

                                        if (isDownloading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                        } else if (translation.isDownloaded) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        translationToDelete = translation
                                                        showDeleteConfirmDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = stringResource(R.string.translations_delete),
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    selectedTranslationToDownload = translation
                                                    showConfirmDialog = true
                                                },
                                                modifier = Modifier.size(width = 80.dp, height = 32.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(stringResource(R.string.translations_get), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(start = 88.dp))
                                }
                            }
                        }
                    }
                    is TranslationsUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.translations_error_loading), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        if (showDeleteConfirmDialog && translationToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    translationToDelete = null
                },
                title = { Text(stringResource(R.string.translations_confirm_delete_title)) },
                text = {
                    Text(stringResource(R.string.translations_confirm_delete_message, translationToDelete?.name ?: ""))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            translationToDelete?.let { translation ->
                                translationsViewModel.deleteTranslation(translation) {
                                    // La UI se actualizará automáticamente
                                }
                            }
                            showDeleteConfirmDialog = false
                            translationToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.translations_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirmDialog = false
                        translationToDelete = null
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    // Search bottom sheet
    if (showSearchSheet) {
        val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()
        val isSearching by searchViewModel.isSearching.collectAsStateWithLifecycle()

        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false; searchViewModel.clearResults() },
            sheetState = searchSheetState,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.search_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.search_examples), style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchViewModel.onQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { searchViewModel.search((uiState as? HomeUiState.Success)?.translationId ?: "spa_bes") }
                    )
                )

                Button(
                    onClick = { searchViewModel.search((uiState as? HomeUiState.Success)?.translationId ?: "spa_bes") },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.search_button)) }

                when {
                    isSearching -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    searchUiState is SearchUiState.NotFound -> Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.search_not_found), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.search_try_examples), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    searchResults.isNotEmpty() -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(searchResults) { result ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${result.bookName} ${result.chapterNumber}:${result.verseNumber}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(result.verseText, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Button(
                                        onClick = {
                                            viewModel.navigateToBook(
                                                context = context,
                                                bookId = result.bookId,
                                                bookName = result.bookName,
                                                chapterNumber = result.chapterNumber,
                                                verseNumber = result.verseNumber
                                            )
                                            scope.launch { searchSheetState.hide() }
                                            showSearchSheet = false
                                            searchViewModel.clearResults()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) { Text(stringResource(R.string.search_go_to_verse)) }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Voice bottom sheet
    if (showVoiceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVoiceSheet = false },
            sheetState = voiceSheetState,
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(stringResource(R.string.voice_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                if (ttsState.availableVoices.isEmpty()) {
                    Text(stringResource(R.string.voice_no_voices), modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(ttsState.availableVoices) { voice ->
                            Clickable(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.setVoice(voice); scope.launch { voiceSheetState.hide() }; showVoiceSheet = false }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(voice.name)
                                    if (ttsState.currentVoice?.name == voice.name) Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
                Clickable(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val intent = android.content.Intent().apply { action = "com.android.settings.TTS_SETTINGS" }
                        context.startActivity(intent)
                        showVoiceSheet = false
                    }
                ) { Text(stringResource(R.string.voice_install_more), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp)) }

                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.voice_speech_settings), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                HorizontalDivider()

                var currentRate by remember { mutableStateOf(ttsState.currentRate) }
                var currentPitch by remember { mutableStateOf(ttsState.currentPitch) }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.voice_speed))
                    Slider(
                        value = currentRate,
                        onValueChange = {
                            currentRate = it
                            viewModel.setSpeechRate(it)
                        },
                        valueRange = 0.5f..2.0f
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(stringResource(R.string.voice_pitch))
                    Slider(
                        value = currentPitch,
                        onValueChange = {
                            currentPitch = it
                            viewModel.setPitch(it)
                        },
                        valueRange = 0.5f..2.0f
                    )
                }
            }
        }
    }

    // Favorites bottom sheet
    if (showFavoritesSheet) {
        val favoriteVerses by viewModel.favoriteVerses.collectAsStateWithLifecycle(initialValue = emptyList())
        val playingFavoriteId by viewModel.playingFavoriteId.collectAsStateWithLifecycle()

        ModalBottomSheet(
            onDismissRequest = {
                showFavoritesSheet = false
                showDeleteFavoriteDialog = false
                favoriteToDelete = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { showFavoritesSheet = false }) { Icon(Icons.Default.Close, null) }
                }
                HorizontalDivider()

                if (favoriteVerses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Text(stringResource(R.string.favorites_empty_title), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = stringResource(R.string.favorites_empty_message),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favoriteVerses) { favorite ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.navigateToBook(
                                        context = context,
                                        bookId = favorite.bookId,
                                        bookName = favorite.bookName,
                                        chapterNumber = favorite.chapterNumber,
                                        verseNumber = favorite.verseNumber
                                    )
                                    showFavoritesSheet = false
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${favorite.bookName} ${favorite.chapterNumber}:${favorite.verseNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(favorite.verseText.take(100) + if (favorite.verseText.length > 100) "..." else "", maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(favorite.dateAdded)), style = MaterialTheme.typography.labelSmall)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val isPlaying = playingFavoriteId == favorite.id
                                        IconButton(onClick = {
                                            if (isPlaying) viewModel.stopFavoritePlayback()
                                            else viewModel.playFavoriteVerse(favorite)
                                        }) {
                                            Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, null, tint = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            favoriteToDelete = favorite
                                            showDeleteFavoriteDialog = true
                                        }) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterContent(
    verses: List<ChapterVerse>,
    scrollToVerse: Int,
    onVerseVisible: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    favoriteIds: Set<String>,
    currentBookId: String,
    currentChapterNumber: Int,
    highlightStart: Int = -1,
    highlightEnd: Int = -1,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToVerse) {
        val index = verses.indexOfFirst { it.number == scrollToVerse }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    val verseOffsets = remember(verses) {
        var offset = 0
        verses.map { verse ->
            val text = verse.content.joinToString(" ") { it.toText() }
            val start = offset
            offset += text.length + 1
            start
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    ) {
        itemsIndexed(verses) { index, verse ->
            val favoriteKey = "${currentBookId}_${currentChapterNumber}_${verse.number}"
            VerseItem(
                verse = verse,
                onVisible = { onVerseVisible(verse.number) },
                onFavoriteClick = { onFavoriteClick(verse.number) },
                isFavorite = favoriteIds.contains(favoriteKey),
                highlightStart = highlightStart,
                highlightEnd = highlightEnd,
                verseOffset = verseOffsets.getOrElse(index) { 0 },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VerseItem(
    verse: ChapterVerse,
    onVisible: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean,
    highlightStart: Int = -1,
    highlightEnd: Int = -1,
    verseOffset: Int = 0,
) {
    LaunchedEffect(verse.number) { onVisible() }

    val verseText = verse.content.joinToString(" ") { it.toText() }
    val verseStart = verseOffset
    val verseEnd = verseOffset + verseText.length
    val localStart = (highlightStart - verseStart).coerceAtLeast(0)
    val localEnd = (highlightEnd - verseStart).coerceAtMost(verseText.length)
    val isHighlighted = highlightStart >= verseStart && highlightStart < verseEnd

    val text = buildAnnotatedString {
        withStyle(SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)) {
            append("${verse.number} ")
        }
        if (isHighlighted && localEnd > localStart) {
            append(verseText.substring(0, localStart))
            withStyle(SpanStyle(background = MaterialTheme.colorScheme.primaryContainer, color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                append(verseText.substring(localStart, localEnd))
            }
            append(verseText.substring(localEnd))
        } else {
            append(verseText)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 36.sp,
            fontSize = 22.sp,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) stringResource(R.string.favorites_remove) else stringResource(R.string.home_favorites),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
