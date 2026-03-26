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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
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

    // Track if initial navigation has been handled
    var initialNavigationHandled by remember { mutableStateOf(false) }

    // Navigate to specific verse when coming from SplashScreen
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
                // Load default book (Genesis 1)
                viewModel.navigateToLastReadVerse()
            }
            initialNavigationHandled = true
        }
    }

    // Change translation when coming from SplashScreen
    LaunchedEffect(selectedTranslationId) {
        if (selectedTranslationId != null) {
            Log.d("HomeScreen", "Changing translation to: $selectedTranslationId")
            viewModel.changeTranslation(selectedTranslationId)
            onTranslationChanged()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.initTts(context)
        onDispose { }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()
    val booksUiState by booksViewModel.uiState.collectAsStateWithLifecycle()
    val translationsUiState by translationsViewModel.uiState.collectAsStateWithLifecycle()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by searchViewModel.query.collectAsStateWithLifecycle()

    // Bottom sheet states
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is HomeUiState.Success -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // Book + chapter button
                                TextButton(
                                    onClick = { showBooksSheet = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "${state.bookName} ${state.chapterNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                // Translation badge
                                Clickable(onClick = { showTranslationsSheet = true }) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.small,
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = state.translationId.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }
                        else -> Text("KAIROS")
                    }
                },
                actions = {
                    // Search button
                    IconButton(onClick = { showSearchSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Voice button
                    IconButton(onClick = { showVoiceSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Voice",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column {
                // Ad placeholder
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
                // Chapter navigation
                when (val state = uiState) {
                    is HomeUiState.Success -> Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = { viewModel.navigatePreviousChapter() },
                            enabled = state.hasPrevious,
                        ) {
                            Text("< ${state.chapterNumber - 1}".takeIf { state.hasPrevious } ?: "<")
                        }
                        FilledTonalIconButton(
                            onClick = {
                                if (ttsState.isPlaying) viewModel.stopSpeaking()
                                else viewModel.speakCurrentChapter()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                        ) {
                            Icon(
                                imageVector = if (ttsState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (ttsState.isPlaying) "Stop" else "Play",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        TextButton(
                            onClick = { viewModel.navigateNextChapter() },
                            enabled = state.hasNext,
                        ) {
                            Text("${state.chapterNumber + 1} >")
                        }
                    }
                    else -> {}
                }
                // Bottom action bar
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Books
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { showBooksSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Books",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Books",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Search
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Daily verse
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { showDailyVerseSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Verse of the Day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Translations
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { showTranslationsSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translations",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Version",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Voice
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { showVoiceSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Voice",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Voice",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                is HomeUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
                is HomeUiState.Empty -> Text(
                    text = "No verses available",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                is HomeUiState.Success -> ChapterContent(
                    verses = state.verses,
                    scrollToVerse = state.scrollToVerse,
                    onVerseVisible = { viewModel.onVerseVisible(it) },
                    highlightStart = ttsState.highlightStart,
                    highlightEnd = ttsState.highlightEnd,
                )
                is HomeUiState.Error -> Text(
                    text = "Error: ${state.message}",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
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
                Text(
                    text = "Verse of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                HorizontalDivider()
                when (val state = dailyUiState) {
                    is mobi.kairos.android.ui.splash.SplashUiState.Loading ->
                        CircularProgressIndicator()
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
                                // Navigate back to SplashScreen
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
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showBooksSheet = false }) {
                        Text("Cancel")
                    }
                    Text(
                        text = "Books",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    // Sort toggle
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
                                        Clickable(onClick = {
                                            expandedBookId = if (expandedBookId == book.id) null else book.id
                                        }) {
                                            Text(
                                                text = if (expandedBookId == book.id) "∧" else "∨",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(8.dp),
                                            )
                                        }
                                    }
                                    if (expandedBookId == book.id) {
                                        // Chapters grid
                                        val chapters = (book.firstChapterNumber..<book.firstChapterNumber + book.numberOfChapters).toList()
                                        val rows = chapters.chunked(5)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 32.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            rows.forEach { rowChapters ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
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
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Text(
                                                                    text = "$chapter",
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                )
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
                    TextButton(onClick = { showTranslationsSheet = false }) {
                        Text("Cancel")
                    }
                    Text(
                        text = "My Versions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.size(64.dp))
                }
                HorizontalDivider()
                when (val state = translationsUiState) {
                    is mobi.kairos.android.ui.translations.TranslationsUiState.Success -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(state.translations) { translation ->
                                Clickable(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        viewModel.changeTranslation(translation.id)
                                        scope.launch { translationsSheetState.hide() }
                                        showTranslationsSheet = false
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = translation.shortName.take(4),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
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
                                }
                                HorizontalDivider(modifier = Modifier.padding(start = 88.dp))
                            }
                        }
                    }
                    else -> CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(32.dp),
                    )
                }
            }
        }
    }

    // Search bottom sheet
    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
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
                    text = "Example: Genesis 3:3",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchViewModel.onQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Genesis 3:3") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            searchViewModel.search(
                                (uiState as? HomeUiState.Success)?.translationId ?: "spa_bes"
                            )
                        },
                    ),
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
                when (val state = searchUiState) {
                    is mobi.kairos.android.ui.search.SearchUiState.Loading ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is mobi.kairos.android.ui.search.SearchUiState.NotFound ->
                        Text(
                            text = "Verse not found",
                            color = MaterialTheme.colorScheme.error,
                        )
                    is mobi.kairos.android.ui.search.SearchUiState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${state.result.bookName} ${state.result.chapterNumber}:${state.result.verseNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(text = state.result.verseText)
                            Button(
                                onClick = {
                                    viewModel.navigateToBook(
                                        state.result.bookId,
                                        state.result.bookName,
                                        state.result.chapterNumber,
                                        state.result.verseNumber,
                                    )
                                    scope.launch { searchSheetState.hide() }
                                    showSearchSheet = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Go to Verse")
                            }
                        }
                    }
                    else -> {}
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
            Column(
                modifier = Modifier.padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Select Voice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                )
                HorizontalDivider()
                if (ttsState.availableVoices.isEmpty()) {
                    Text(
                        text = "No voices available",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = voice.name)
                                    if (ttsState.currentVoice?.name == voice.name) {
                                        Text(
                                            text = "✓",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
                // Install more voices
                Clickable(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val intent = android.content.Intent()
                        intent.action = "com.android.settings.TTS_SETTINGS"
                        context.startActivity(intent)
                        showVoiceSheet = false
                    },
                ) {
                    Text(
                        text = "+ Install more voices",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp),
                    )
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
            VerseItem(
                verse = verse,
                onVisible = { onVerseVisible(verse.number) },
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

    val text = buildAnnotatedString {
        withStyle(SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.Gray)) {
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

    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 36.sp,
        fontSize = 22.sp,
        modifier = Modifier.fillMaxWidth(),
    )
}
