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
package mobi.kairos.android.data

import junit.framework.TestCase
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import mobi.kairos.android.data.di.dataModule
import mobi.kairos.android.di.domainModule
import mobi.kairos.android.usecase.ImportTranslationsUseCase

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FullIntegrationTest : KoinTest {
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val testModule = module {
            single<AppDatabase> {
                Room.inMemoryDatabaseBuilder(
                    context,
                    AppDatabase::class.java,
                ).allowMainThreadQueries()
                    .build()
            }
        }

        startKoin {
            androidContext(context)
            allowOverride(true)
            modules(domainModule, dataModule, testModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun fullImportFlowWithRealDatabase() = runTest {
        // Given
        val useCase: ImportTranslationsUseCase = get()

        // When
        val result = useCase()

        // Then
        result.fold(
            onSuccess = { summary ->
                println("✅ Imported ${summary.count} translations")
                // Verify DB
                // val database: AppDatabase = get()
                // val dao = database.translationDao()
                // val count = dao.getCount()
                // TestCase.assertEquals(summary.count, count)
            },
            onFailure = { error ->
                TestCase.fail("Failed: ${error.message}")
            },
        )
    }
}
