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

import mobi.kairos.android.repository.FavoritesRepository
import mobi.kairos.android.repository.TranslationBookRepository
import mobi.kairos.android.usecase.GetLastReadVerseUseCase
import mobi.kairos.android.usecase.GetOrDownloadChapterUseCase
import mobi.kairos.android.usecase.GetVersesUseCase
import mobi.kairos.android.usecase.SaveLastReadVerseUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import mobi.kairos.android.data.repository.TtsPreferencesRepository

val homeModule = module {
    viewModel<HomeViewModel> {
        HomeViewModel(
            get<GetLastReadVerseUseCase>(),
            get<GetVersesUseCase>(),
            get<SaveLastReadVerseUseCase>(),
            get<TranslationBookRepository>(),
            get<GetOrDownloadChapterUseCase>(),
            get<FavoritesRepository>(),
            get<TtsPreferencesRepository>()
        )
    }
}
