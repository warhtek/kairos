package mobi.kairos.android.ui.home

import mobi.kairos.android.repository.TranslationBookRepository
import mobi.kairos.android.usecase.GetLastReadVerseUseCase
import mobi.kairos.android.usecase.GetVersesUseCase
import mobi.kairos.android.usecase.SaveLastReadVerseUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel<HomeViewModel> {
        HomeViewModel(
            get<GetLastReadVerseUseCase>(),
            get<GetVersesUseCase>(),
            get<SaveLastReadVerseUseCase>(),
            get<TranslationBookRepository>(),
        )
    }
}
