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
import android.util.Log
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
import mobi.kairos.android.usecase.ImportTranslationBooksUseCase
import mobi.kairos.android.usecase.ImportTranslationsUseCase

private const val TAG = "FullAppDatabaseImportIntegrationTest"

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FullAppDatabaseImportIntegrationTest : KoinTest {
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val testModule = module {
            single<AppDatabase> {
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
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
    fun verifyImportTranslation() = runTest {
        // Given
        val database: AppDatabase = get()
        val useCase: ImportTranslationsUseCase = get()
        val dao = database.translationDao()

        // When
        val result = useCase()
        val count = dao.count()

        // Then
        result.fold(
            onSuccess = { summary ->
                TestCase.assertEquals(summary.count, count)
                Log.d(TAG, "✅ Imported ${summary.count} translations")
                TestCase.assertEquals(summary.count, 1255)
            },
            onFailure = { error ->
                TestCase.fail("Failed: ${error.message}")
            },
        )
    }

    @Test
    fun verifyImportTranslationBooks() = runTest {
        // Given
        val database: AppDatabase = get()
        val useCase: ImportTranslationBooksUseCase = get()
        val dao = database.translationBookDao()

        // When
        val result = useCase()
        val count = dao.count()

        // Then
        result.fold(
            onSuccess = { summary ->
                TestCase.assertEquals(summary.count, count)
                Log.d(TAG, "✅ Imported ${summary.count} translation books")
                TestCase.assertEquals(summary.count, 66)
            },
            onFailure = { error ->
                TestCase.fail("Failed: ${error.message}")
            },
        )
    }
}
