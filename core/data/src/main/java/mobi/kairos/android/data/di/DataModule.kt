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
package mobi.kairos.android.data.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import mobi.kairos.android.data.AppDatabase
import mobi.kairos.android.data.RoomReadyNotifier
import mobi.kairos.android.data.dao.DatabaseInfoDao
import mobi.kairos.android.data.dao.TranslationDao
import mobi.kairos.android.data.parser.TranslationJsonParserImpl
import mobi.kairos.android.data.repository.DatabaseRepositoryImpl
import mobi.kairos.android.data.repository.TranslationRepositoryImpl
import mobi.kairos.android.data.resource.AndroidAssetResource
import mobi.kairos.android.data.resource.TranslationsAssetImpl
import mobi.kairos.android.parser.TranslationJsonParser
import mobi.kairos.android.repository.DatabaseRepository
import mobi.kairos.android.repository.TranslationRepository
import mobi.kairos.android.resource.AssetResource
import mobi.kairos.android.resource.TranslationsAsset

val dataModule =
    module {
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
        single<RoomReadyNotifier> { RoomReadyNotifier(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<TranslationRepository> { TranslationRepositoryImpl(get()) }
        single<DatabaseInfoDao> { get<AppDatabase>().databaseInfoDao() }
        single<TranslationDao> { get<AppDatabase>().translationDao() }
        single<AssetResource> { AndroidAssetResource(androidContext()) }
        single<TranslationsAsset> { TranslationsAssetImpl(get()) }
        single<TranslationRepository> { TranslationRepositoryImpl(get()) }
        single<TranslationJsonParser> { TranslationJsonParserImpl() }
        includes(roomModule)
    }
