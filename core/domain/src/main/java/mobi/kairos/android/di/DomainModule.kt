package mobi.kairos.android.di

import org.koin.dsl.module
import mobi.kairos.android.usecase.GetBooksUseCase
import mobi.kairos.android.usecase.GetDatabaseVersionUseCase
import mobi.kairos.android.usecase.GetChapterUseCase
import mobi.kairos.android.usecase.GetLastReadVerseUseCase
import mobi.kairos.android.usecase.GetTranslationsUseCase
import mobi.kairos.android.usecase.ImportTranslationBooksUseCase
import mobi.kairos.android.usecase.ImportTranslationsUseCase
import mobi.kairos.android.usecase.GetVersesUseCase
import mobi.kairos.android.usecase.SaveLastReadVerseUseCase

val domainModule =
    module {
        factory<GetDatabaseVersionUseCase> { GetDatabaseVersionUseCase(get()) }
        factory<ImportTranslationsUseCase> { ImportTranslationsUseCase(get(), get(), get()) }
        factory<ImportTranslationBooksUseCase> { ImportTranslationBooksUseCase(get(), get(), get()) }
        factory<GetChapterUseCase> { GetChapterUseCase(get()) }
        factory<GetLastReadVerseUseCase> { GetLastReadVerseUseCase(get()) }
        factory<GetBooksUseCase> { GetBooksUseCase(get()) }
        factory<GetVersesUseCase> { GetVersesUseCase(get()) }
        factory<SaveLastReadVerseUseCase> { SaveLastReadVerseUseCase(get()) }
        factory<GetTranslationsUseCase> { GetTranslationsUseCase(get()) }
    }
