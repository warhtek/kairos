package mobi.kairos.android.data.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import mobi.kairos.android.repository.DatabaseRepository
import mobi.kairos.android.repository.TranslationRepository
import mobi.kairos.android.repository.TranslationBookRepository
import mobi.kairos.android.repository.ChapterRepository
import mobi.kairos.android.repository.ReadingProgressRepository
import mobi.kairos.android.data.repository.DatabaseRepositoryImpl
import mobi.kairos.android.data.repository.TranslationRepositoryImpl
import mobi.kairos.android.data.repository.TranslationBookRepositoryImpl
import mobi.kairos.android.data.repository.ChapterRepositoryImpl
import mobi.kairos.android.data.repository.ReadingProgressRepositoryImpl
import mobi.kairos.android.data.resource.AndroidAssetResource
import mobi.kairos.android.data.resource.TranslationsAssetImpl
import mobi.kairos.android.data.resource.TranslationBooksAssetImpl
import mobi.kairos.android.data.resource.CompleteTranslationAssetImpl
import mobi.kairos.android.resource.AssetResource
import mobi.kairos.android.resource.TranslationsAsset
import mobi.kairos.android.resource.TranslationBooksAsset
import mobi.kairos.android.resource.CompleteTranslationAsset
import mobi.kairos.android.data.AppDatabase
import mobi.kairos.android.data.databaseBuilder
import mobi.kairos.android.data.RoomNotifier
import mobi.kairos.android.data.TranslationDownloader
import mobi.kairos.android.data.dao.DownloadedTranslationDao
import mobi.kairos.android.data.dao.FavoriteVerseDao
import mobi.kairos.android.repository.FavoritesRepository
import mobi.kairos.android.data.repository.FavoritesRepositoryImpl

val dataModule = module {
    // Database
    single { RoomNotifier() }
    single<AppDatabase> { databaseBuilder(androidContext(), "kairos.db", get()) }
    single { get<AppDatabase>().databaseInfoDao() }
    single { get<AppDatabase>().translationDao() }
    single { get<AppDatabase>().translationBookDao() }
    single { get<AppDatabase>().chapterDao() }
    single { get<AppDatabase>().readingProgressDao() }
    single<DownloadedTranslationDao> { get<AppDatabase>().downloadedTranslationDao() }

    // Asset Resource
    single<AssetResource> { AndroidAssetResource(androidContext()) }

    // Assets
    single<TranslationsAsset> { TranslationsAssetImpl(get()) }
    single<TranslationBooksAsset> { TranslationBooksAssetImpl(get()) }
    single<CompleteTranslationAsset> { CompleteTranslationAssetImpl(get()) }

    // Repositorios
    single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
    single<TranslationRepository> { TranslationRepositoryImpl(get()) }
    single<TranslationBookRepository> { TranslationBookRepositoryImpl(get()) }
    single<ChapterRepository> { ChapterRepositoryImpl(get()) }
    single<ReadingProgressRepository> { ReadingProgressRepositoryImpl(get()) }
    single { get<AppDatabase>().favoriteVerseDao() }
    single<FavoritesRepository> { FavoritesRepositoryImpl(get()) }

    single { TranslationDownloader(androidContext(), get(), get(), get(), get()) }}
