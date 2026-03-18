package mobi.kairos.android.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.usecase.GetBooksUseCase

class BooksViewModel(
    private val getBooks: GetBooksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            getBooks()
                .onSuccess { books ->
                    _uiState.value = if (books.isEmpty()) {
                        BooksUiState.Empty
                    } else {
                        BooksUiState.Success(books)
                    }
                }
                .onFailure {
                    _uiState.value = BooksUiState.Error(it.message ?: "Unknown error")
                }
        }
    }
}

sealed class BooksUiState {
    object Loading : BooksUiState()
    object Empty : BooksUiState()
    data class Success(val books: List<TranslationBook>) : BooksUiState()
    data class Error(val message: String) : BooksUiState()
}
