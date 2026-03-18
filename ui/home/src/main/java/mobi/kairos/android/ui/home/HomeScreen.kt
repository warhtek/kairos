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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobi.kairos.android.ui.common.Verse
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource

@Composable
fun HomeScreen(
    onNavigateToBooks: () -> Unit,
    selectedBookId: String? = null,
    selectedBookName: String? = null,
    onBookSelected: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    if (selectedBookId != null && selectedBookName != null) {
        viewModel.navigateToBook(selectedBookId, selectedBookName)
        onBookSelected()
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
        topBar = { KairosTopBar() },
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
                        text = "Publicidad",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                when (val state = uiState) {
                    is HomeUiState.Success -> Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.navigatePrevious() },
                            enabled = state.hasPrevious,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                        ) {
                            Text("← Anterior")
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateNext() },
                            enabled = state.hasNext,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                        ) {
                            Text("Siguiente →")
                        }
                    }
                    else -> {}
                }
                Button(
                    onClick = onNavigateToBooks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.read_books))//Text("Leer la Biblia")
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(
                    onClick = {
                        if (ttsState.isPlaying) viewModel.stopSpeaking()
                        else viewModel.speakCurrentVerse()
                    },
                ) {
                    Text(if (ttsState.isPlaying) "■" else "▶")
                }
                Box {
                    TextButton(onClick = { showVoiceMenu = true }) {
                        Text(
                            text = "Voz: ${ttsState.currentVoice?.name?.take(20) ?: "Default"}",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    DropdownMenu(
                        expanded = showVoiceMenu,
                        onDismissRequest = { showVoiceMenu = false },
                    ) {
                        if (ttsState.availableVoices.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay voces disponibles") },
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
                    }
                }
                TextButton(
                    onClick = {
                        val intent = android.content.Intent()
                        intent.action = "com.android.settings.TTS_SETTINGS"
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "+ Voces",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> CircularProgressIndicator()

                    is HomeUiState.Empty -> Text(
                        text = "No hay versículos disponibles",
                        modifier = Modifier.padding(24.dp),
                    )

                    is HomeUiState.Success -> Verse(
                        modifier = Modifier.padding(24.dp),
                        title = "${state.verse.bookName} ${state.verse.chapterNumber}:${state.verse.verseNumber}",
                        text = state.verse.verseText,
                        onClick = {},
                    )

                    is HomeUiState.Error -> Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}
