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

import org.koin.dsl.module
import mobi.kairos.android.usecase.GetDatabaseVersionUseCase
import mobi.kairos.android.usecase.ImportTranslationBooksUseCase
import mobi.kairos.android.usecase.ImportTranslationsUseCase

val domainModule =
    module {
        factory { GetDatabaseVersionUseCase(get()) }
        factory { ImportTranslationsUseCase(get(), get(), get()) }
        factory { ImportTranslationBooksUseCase(get(), get(), get()) }
    }
