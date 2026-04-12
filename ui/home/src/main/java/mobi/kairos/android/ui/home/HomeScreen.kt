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

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mobi.kairos.android.model.ChapterVerse
import mobi.kairos.android.ui.books.BooksViewModel
import mobi.kairos.android.ui.common.Clickable
import mobi.kairos.android.ui.search.SearchViewModel
import mobi.kairos.android.ui.splash.SplashViewModel
import mobi.kairos.android.ui.translations.TranslationsViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.heightIn
import mobi.kairos.android.ui.search.SearchUiState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.text.style.TextOverflow


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

    LaunchedEffect(selectedBookId, selectedBookName, selectedChapterNumber, selectedVerseNumber) {
        if (!initialNavigationHandled) {
            if (selectedBookId != null && selectedBookName != null) {
                Log.d("HomeScreen", "Navigating to specific verse: $selectedBookName $selectedChapterNumber:$selectedVerseNumber")
                viewModel.navigateToBook(
                    selectedBookId,
                    selectedBookName,
                    selectedChapterNumber,
                    selectedVerseNumber,
                )
                onBookSelected()
            } else {
                Log.d("HomeScreen", "No specific verse selected, loading default book")
                viewModel.navigateToLastReadVerse()
            }
            initialNavigationHandled = true
        }
    }

    LaunchedEffect(selectedTranslationId) {
        if (selectedTranslationId != null) {
            Log.d("HomeScreen", "Changing translation to: $selectedTranslationId")
            viewModel.changeTranslation(selectedTranslationId)
            onTranslationChanged()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        translationsViewModel.setContext(context)
        viewModel.initTtsForFavorites(context)
    }

    DisposableEffect(Unit) {
        viewModel.initTts(context)
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

    fun getTranslationSize(translationId: String?): String {
        return when (translationId) {
            "spa_bes" -> "5.2"
            "BSB" -> "6.8"
            "AAB" -> "4.5"
            "ARBNAV" -> "8.2"
            "HINIRV" -> "7.1"
            else -> "3.0"
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        when (val state = uiState) {
                            is HomeUiState.Success -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.navigatePreviousChapter() },
                                            enabled = state.hasPrevious,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SkipPrevious,
                                                    contentDescription = "Previous Chapter",
                                                    modifier = Modifier.size(24.dp),
                                                    tint = if (state.hasPrevious) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                )
                                                Text(
                                                    text = "${state.chapterNumber - 1}".takeIf { state.hasPrevious } ?: "",
                                                    fontSize = 14.sp,
                                                )
                                            }
                                        }

                                        FilledTonalIconButton(
                                            onClick = {
                                                if (ttsState.isPlaying) {
                                                    viewModel.stopSpeaking()
                                                } else {
                                                    viewModel.ensureTtsReady(context)
                                                    viewModel.speakCurrentChapter()
                                                }
                                            },
                                            modifier = Modifier.size(40.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (ttsState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = if (ttsState.isPlaying) "Stop" else "Play",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        TextButton(
                                            onClick = { viewModel.navigateNextChapter() },
                                            enabled = state.hasNext,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "${state.chapterNumber + 1}",
                                                    fontSize = 14.sp,
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.SkipNext,
                                                    contentDescription = "Next Chapter",
                                                    modifier = Modifier.size(24.dp),
                                                    tint = if (state.hasNext) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(0.dp))
                                }
                            }
                            else -> Text("KAIROS")
                        }
                    },
                    actions = {
                        val currentState = uiState
                        if (currentState is HomeUiState.Success) {
                            IconButton(onClick = { showFavoritesSheet = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Favorites",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(text = "Favorites", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(9.dp))
                            Clickable(onClick = { showTranslationsSheet = true }) {
                                Text(
                                    text = currentState.translationId.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                )
                when (val state = uiState) {
                    is HomeUiState.Success -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            TextButton(
                                onClick = { showBooksSheet = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.bookName} ${state.chapterNumber}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    else -> {}
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
                    Text(
                        text = "Advertisement",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "Search", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { showBooksSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Books",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "Books", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { showDailyVerseSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Verse of the Day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { showTranslationsSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translations",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "Version", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { showVoiceSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Voice",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "Voice", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is HomeUiState.Empty -> Text(text = "No verses available", modifier = Modifier.align(Alignment.Center).padding(24.dp))
                is HomeUiState.Success -> ChapterContent(
                    verses = state.verses,
                    scrollToVerse = state.scrollToVerse,
                    onVerseVisible = { viewModel.onVerseVisible(it) },
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    favoriteIds = favoriteIds,
                    currentBookId = state.bookId,
                    currentChapterNumber = state.chapterNumber,
                    highlightStart = ttsState.highlightStart,
                    highlightEnd = ttsState.highlightEnd,
                )
                is HomeUiState.Error -> Text(text = "Error: ${state.message}", modifier = Modifier.align(Alignment.Center).padding(24.dp))
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
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Verse of the Day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                when (val state = dailyUiState) {
                    is mobi.kairos.android.ui.splash.SplashUiState.Loading -> CircularProgressIndicator()
                    is mobi.kairos.android.ui.splash.SplashUiState.Success -> {
                        Text(
                            text = "${state.verse.bookName} ${state.verse.chapterNumber}:${state.verse.verseNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "\"${state.verse.verseText}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Button(
                            onClick = {
                                onNavigateToSplash()
                                showDailyVerseSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("View Verse of the Day")
                        }
                    }
                    else -> Text("Not available")
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showBooksSheet = false }) { Text("Cancel") }
                    Text(text = "Books", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { sortAlphabetically = !sortAlphabetically }) {
                        Text(if (sortAlphabetically) "Traditional" else "Alphabetical")
                    }
                }
                HorizontalDivider()
                when (val state = booksUiState) {
                    is mobi.kairos.android.ui.books.BooksUiState.Loading ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is mobi.kairos.android.ui.books.BooksUiState.Success -> {
                        val books = if (sortAlphabetically) state.books.sortedBy { it.name }
                        else state.books.sortedBy { it.order }
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(books) { book ->
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = book.name.uppercase(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (expandedBookId == book.id) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f),
                                        )
                                        IconButton(
                                            onClick = {
                                                expandedBookId = if (expandedBookId == book.id) null else book.id
                                            },
                                            modifier = Modifier.size(48.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Icon(
                                                imageVector = if (expandedBookId == book.id)
                                                    Icons.Default.ArrowCircleUp
                                                else
                                                    Icons.Default.ArrowCircleDown,
                                                contentDescription = if (expandedBookId == book.id) "Collapse" else "Expand",
                                                modifier = Modifier.size(32.dp),
                                            )
                                        }
                                    }
                                    if (expandedBookId == book.id) {
                                        val chapters = (book.firstChapterNumber..<book.firstChapterNumber + book.numberOfChapters).toList()
                                        val rows = chapters.chunked(5)
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            rows.forEach { rowChapters ->
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    rowChapters.forEach { chapter ->
                                                        Clickable(
                                                            modifier = Modifier
                                                                .size(52.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                                            onClick = {
                                                                viewModel.navigateToBook(book.id, book.name, chapter, 1)
                                                                scope.launch { booksSheetState.hide() }
                                                                showBooksSheet = false
                                                            },
                                                        ) {
                                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                                Text(text = "$chapter", style = MaterialTheme.typography.bodyMedium)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
        val downloadingId by translationsViewModel.downloadingTranslation.collectAsStateWithLifecycle()
        val selectedId by translationsViewModel.selectedTranslationId.collectAsStateWithLifecycle()

        ModalBottomSheet(
            onDismissRequest = { showTranslationsSheet = false },
            sheetState = translationsSheetState,
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showTranslationsSheet = false }) { Text("Cancel") }
                    Text(text = "My Versions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.size(64.dp))
                }
                HorizontalDivider()
                when (val state = translationsUiState) {
                    is mobi.kairos.android.ui.translations.TranslationsUiState.Success -> {
                        val downloadedTranslations = state.translations.filter { it.isDownloaded }

                        if (downloadedTranslations.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No downloaded versions available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(downloadedTranslations) { translation ->
                                    val isSelected = selectedId == translation.id
                                    Clickable(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            translationsViewModel.selectTranslation(translation.id)
                                            viewModel.changeTranslation(translation.id)
                                            onTranslationChanged()
                                            scope.launch { translationsSheetState.hide() }
                                            showTranslationsSheet = false
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp, horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            if (isSelected)
                                                                MaterialTheme.colorScheme.primaryContainer
                                                            else
                                                                MaterialTheme.colorScheme.secondaryContainer
                                                        ),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = translation.shortName.take(4),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSecondaryContainer,
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = translation.languageName ?: translation.language,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Text(
                                                        text = translation.name,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Medium,
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "✓",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(start = 88.dp))
                                }
                            }
                        }
                    }
                    else -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
                }
            }
        }
    }

    // Search bottom sheet
    if (showSearchSheet) {
        val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()
        val isSearching by searchViewModel.isSearching.collectAsStateWithLifecycle()

        ModalBottomSheet(
            onDismissRequest = {
                showSearchSheet = false
                searchViewModel.clearResults()
            },
            sheetState = searchSheetState,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Search Verse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Examples: Salmos 3:3, Salmos 3, amor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchViewModel.onQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by verse, chapter or word...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            searchViewModel.search(
                                (uiState as? HomeUiState.Success)?.translationId ?: "spa_bes"
                            )
                        }
                    )
                )

                Button(
                    onClick = {
                        searchViewModel.search(
                            (uiState as? HomeUiState.Success)?.translationId ?: "spa_bes"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Search")
                }

                when {
                    isSearching -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    searchUiState is SearchUiState.NotFound -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Verse not found",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Try: Genesis 1:1, Salmos 23, or a word like 'amor'",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                    searchResults.isNotEmpty() -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 400.dp)
                        ) {
                            items(searchResults) { result ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${result.bookName} ${result.chapterNumber}:${result.verseNumber}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = result.verseText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 3,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Button(
                                            onClick = {
                                                viewModel.navigateToBook(
                                                    result.bookId,
                                                    result.bookName,
                                                    result.chapterNumber,
                                                    result.verseNumber,
                                                )
                                                scope.launch { searchSheetState.hide() }
                                                showSearchSheet = false
                                                searchViewModel.clearResults()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text("Go to Verse")
                                        }
                                    }
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
                Text(text = "Select Voice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                if (ttsState.availableVoices.isEmpty()) {
                    Text(text = "No voices available", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn {
                        items(ttsState.availableVoices) { voice ->
                            Clickable(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.setVoice(voice)
                                    scope.launch { voiceSheetState.hide() }
                                    showVoiceSheet = false
                                },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = voice.name)
                                    if (ttsState.currentVoice?.name == voice.name) {
                                        Text(text = "✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
                Clickable(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val intent = android.content.Intent()
                        intent.action = "com.android.settings.TTS_SETTINGS"
                        context.startActivity(intent)
                        showVoiceSheet = false
                    },
                ) {
                    Text(text = "+ Install more voices", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
    // Favorites bottom sheet
    if (showFavoritesSheet) {
        val favoriteVerses by viewModel.favoriteVerses.collectAsStateWithLifecycle(initialValue = emptyList())
        val playingFavoriteId by viewModel.playingFavoriteId.collectAsStateWithLifecycle()

        ModalBottomSheet(
            onDismissRequest = { showFavoritesSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Favorite Verses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { showFavoritesSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Content
                if (favoriteVerses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "No favorites",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No favorite verses yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap the heart icon next to any verse to add it to your favorites",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.navigateToBook(
                                            favorite.bookId,
                                            favorite.bookName,
                                            favorite.chapterNumber,
                                            favorite.verseNumber
                                        )
                                        showFavoritesSheet = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${favorite.bookName} ${favorite.chapterNumber}:${favorite.verseNumber}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = favorite.verseText.take(100) + if (favorite.verseText.length > 100) "..." else "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                                .format(java.util.Date(favorite.dateAdded)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Botón Play/Stop
                                        val isPlaying = playingFavoriteId == favorite.id

                                        IconButton(
                                            onClick = {
                                                if (isPlaying) {
                                                    viewModel.stopFavoritePlayback()
                                                } else {
                                                    viewModel.playFavoriteVerse(favorite)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Stop" else "Play verse",
                                                tint = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        // Botón Eliminar
                                        IconButton(
                                            onClick = {
                                                viewModel.removeFavoriteFromList(favorite)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove from favorites",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
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
    LaunchedEffect(verse.number) {
        onVisible()
    }

    val verseText = verse.content.joinToString(" ") { it.toText() }
    val verseStart = verseOffset
    val verseEnd = verseOffset + verseText.length
    val localStart = (highlightStart - verseStart).coerceAtLeast(0)
    val localEnd = (highlightEnd - verseStart).coerceAtMost(verseText.length)
    val isHighlighted = highlightStart >= verseStart && highlightStart < verseEnd

    LaunchedEffect(highlightStart, highlightEnd, verse.number) {
        Log.d("VerseItem", "Verse ${verse.number}: verseStart=$verseStart, verseEnd=$verseEnd, highlightStart=$highlightStart, highlightEnd=$highlightEnd, localStart=$localStart, localEnd=$localEnd")
    }

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
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
