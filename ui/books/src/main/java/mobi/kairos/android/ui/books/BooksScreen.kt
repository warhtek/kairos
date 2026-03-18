package mobi.kairos.android.ui.books

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.ui.common.Clickable
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onBookClick: (String, String) -> Unit,
    viewModel: BooksViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.books)) })
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
                is BooksUiState.Success -> BooksList(
                    books = state.books,
                    onBookClick = onBookClick,
                )
            }
        }
    }
}

@Composable
private fun BooksList(
    books: List<TranslationBook>,
    onBookClick: (String, String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(books) { book ->
            Clickable(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onBookClick(book.id, book.name) },
            ) {
                Text(
                    text = book.name,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
    }
}
