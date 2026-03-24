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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobi.kairos.android.model.ChapterVerse
import org.koin.androidx.compose.koinViewModel
import mobi.kairos.android.ui.common.Clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBooks: () -> Unit,
    onNavigateToTranslations: () -> Unit,
    onNavigateToSearch: () -> Unit,
    selectedBookId: String? = null,
    selectedBookName: String? = null,
    selectedChapterNumber: Int = 1,
    selectedVerseNumber: Int = 1,
    selectedTranslationId: String? = null,
    onBookSelected: () -> Unit = {},
    onTranslationChanged: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    if (selectedBookId != null && selectedBookName != null) {
        viewModel.navigateToBook(
            selectedBookId,
            selectedBookName,
            selectedChapterNumber,
            selectedVerseNumber,
        )
        onBookSelected()
    }

    if (selectedTranslationId != null) {
        viewModel.changeTranslation(selectedTranslationId)
        onTranslationChanged()
    }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.initTts(context)
        onDispose { }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()
    var showVoiceMenu by remember { mutableStateOf(false) }

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
                                    onClick = onNavigateToBooks,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "${state.bookName} ${state.chapterNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                // Translation badge - navigates to translations screen
                                Clickable(onClick = onNavigateToTranslations) {
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
                    IconButton(onClick = onNavigateToSearch) {
                        Text("🔍", fontSize = 20.sp)
                    }
                    // Voice selector
                    Box {
                        IconButton(onClick = { showVoiceMenu = true }) {
                            Text("🔊", fontSize = 20.sp)
                        }
                        DropdownMenu(
                            expanded = showVoiceMenu,
                            onDismissRequest = { showVoiceMenu = false },
                        ) {
                            if (ttsState.availableVoices.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No voices available") },
                                    onClick = { showVoiceMenu = false },
                                )
                            } else {
                                ttsState.availableVoices.forEach { voice ->
                                    DropdownMenuItem(
                                        text = { Text(voice.name) },
                                        onClick = {
                                            viewModel.setVoice(voice)
                                            showVoiceMenu = false
                                        },
                                    )
                                }
                            }
                            HorizontalDivider()
                            // Install more voices option
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "+ Instalar más voces",
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                },
                                onClick = {
                                    showVoiceMenu = false
                                    val intent = android.content.Intent()
                                    intent.action = "com.android.settings.TTS_SETTINGS"
                                    context.startActivity(intent)
                                },
                            )
                        }
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
                        text = "Publicidad",
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
                        // Previous chapter
                        TextButton(
                            onClick = { viewModel.navigatePreviousChapter() },
                            enabled = state.hasPrevious,
                        ) {
                            Text("< ${state.chapterNumber - 1}".takeIf { state.hasPrevious } ?: "<")
                        }

                        // Play/stop floating button
                        FilledTonalIconButton(
                            onClick = {
                                if (ttsState.isPlaying) viewModel.stopSpeaking()
                                else viewModel.speakCurrentChapter()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                        ) {
                            Text(
                                text = if (ttsState.isPlaying) "■" else "▶",
                                fontSize = 20.sp,
                            )
                        }

                        // Next chapter
                        TextButton(
                            onClick = { viewModel.navigateNextChapter() },
                            enabled = state.hasNext,
                        ) {
                            Text("${state.chapterNumber + 1} >")
                        }
                    }
                    else -> {}
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
                    text = "No hay versículos disponibles",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
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
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )
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

    // Calculate verse offsets for highlight mapping
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

    // Check if highlight is within this verse
    val localStart = (highlightStart - verseStart).coerceAtLeast(0)
    val localEnd = (highlightEnd - verseStart).coerceAtMost(verseText.length)
    val isHighlighted = highlightStart >= verseStart && highlightStart < verseEnd

    val text = buildAnnotatedString {
        // Verse number
        withStyle(
            SpanStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.Gray,
            )
        ) {
            append("${verse.number} ")
        }
        // Verse text with optional highlight
        if (isHighlighted && localEnd > localStart) {
            append(verseText.substring(0, localStart))
            withStyle(
                SpanStyle(
                    background = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ) {
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
