package mobi.kairos.android.ui.books

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import mobi.kairos.android.usecase.GetBooksUseCase

val booksModule = module {
    viewModel<BooksViewModel> { BooksViewModel(get<GetBooksUseCase>()) }
}
