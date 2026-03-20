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
package mobi.kairos.android.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import mobi.kairos.android.data.di.dataModule
import mobi.kairos.android.ui.books.booksModule
import mobi.kairos.android.ui.home.homeModule
import mobi.kairos.android.ui.translations.translationsModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single<CoroutineScope>(named("appScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    includes(domainModule, dataModule, homeModule, booksModule, translationsModule)
}
