package mobi.kairos.android.di

import org.koin.dsl.module
import mobi.kairos.android.usecase.*

val domainModule = module {
    // UseCases con parámetros
    single { GetDatabaseVersionUseCase(get()) }
    single { ImportTranslationsUseCase(get(), get(), get()) }
    single { ImportTranslationBooksUseCase(get(), get(), get()) }
    single { GetChapterUseCase(get()) }
    single { GetLastReadVerseUseCase(get()) }
    single { GetBooksUseCase(get()) }
    single { GetVersesUseCase(get()) }
    single { SaveLastReadVerseUseCase(get()) }
    single { GetOrDownloadChapterUseCase(get()) }  // ← Pasa el ChapterRepository

    // UseCases sin parámetros
    single { GetTranslationsUseCase() }
    single { SearchVerseUseCase() }
    single { GetDailyVerseUseCase() }
}
