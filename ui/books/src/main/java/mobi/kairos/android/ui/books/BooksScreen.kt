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
package mobi.kairos.android.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.ui.common.Clickable
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onBookClick: (String, String, Int) -> Unit,
    viewModel: BooksViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sortAlphabetically by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Libros",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
        bottomBar = {
            // Sort toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    TextButton(
                        onClick = { sortAlphabetically = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (!sortAlphabetically)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                    ) {
                        Text(
                            text = "Tradicional",
                            color = if (!sortAlphabetically)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = { sortAlphabetically = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (sortAlphabetically)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                    ) {
                        Text(
                            text = "Alfabético",
                            color = if (sortAlphabetically)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
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
            contentAlignment = Alignment.Center,
        ) {
            when (val state = uiState) {
                is BooksUiState.Loading -> CircularProgressIndicator()
                is BooksUiState.Empty -> Text("No hay libros disponibles")
                is BooksUiState.Error -> Text("Error: ${state.message}")
                is BooksUiState.Success -> {
                    val books = if (sortAlphabetically) {
                        state.books.sortedBy { it.name }
                    } else {
                        state.books.sortedBy { it.order }
                    }
                    BooksList(
                        books = books,
                        onBookClick = onBookClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun BooksList(
    books: List<TranslationBook>,
    onBookClick: (String, String, Int) -> Unit,
) {
    var expandedBookId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(books) { book ->
            BookItem(
                book = book,
                isExpanded = expandedBookId == book.id,
                onToggle = {
                    expandedBookId = if (expandedBookId == book.id) null else book.id
                },
                onChapterClick = { chapterNumber ->
                    onBookClick(book.id, book.name, chapterNumber)
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun BookItem(
    book: TranslationBook,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onChapterClick: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Book row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = book.name.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Expand toggle
                Clickable(onClick = onToggle) {
                    Text(
                        text = if (isExpanded) "∧" else "∨",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }

        // Chapters grid
        if (isExpanded) {
            ChaptersGrid(
                numberOfChapters = book.numberOfChapters,
                firstChapterNumber = book.firstChapterNumber,
                onChapterClick = onChapterClick,
            )
        }
    }
}

@Composable
private fun ChaptersGrid(
    numberOfChapters: Int,
    firstChapterNumber: Int,
    onChapterClick: (Int) -> Unit,
) {
    val chapters = (firstChapterNumber..firstChapterNumber + numberOfChapters - 1).toList()
    val rows = chapters.chunked(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { rowChapters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowChapters.forEach { chapter ->
                    Clickable(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        onClick = { onChapterClick(chapter) },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$chapter",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}
