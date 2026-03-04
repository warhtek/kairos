/**
 * Copyright (C) 2026 MOBIWARE
 *
 * This software and its source code are the exclusive property of MOBIWARE.
 *
 * Any unauthorized use, copying, distribution, or modification of this software, in whole or in part,
 * may result in severe civil and criminal penalties under applicable copyright and trade secret laws.
 */
package mobi.kairos.android.data.di

import mobi.kairos.android.data.repository.DatabaseRepositoryImpl
import mobi.kairos.android.di.domainModule
import mobi.kairos.android.repository.DatabaseRepository
import org.koin.dsl.module

val dataModule = module {
    includes(domainModule)
    includes(roomModule)
    single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
}
