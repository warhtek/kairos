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

import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import mobi.kairos.android.data.AppDatabase
import mobi.kairos.android.data.databaseBuilder

private const val DB_NAME = "kairos.db"

val roomModule = module {
    single(named("DB_NAME")) { DB_NAME }
    single<AppDatabase> {
        databaseBuilder(
            context = androidContext(),
            dbName = get(named("DB_NAME")),
            notifier = get(),
        )
    }
}
