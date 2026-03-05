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

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertTrue
import mobi.kairos.android.data.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseNameOverrideTest : KoinTest {
    @Before
    fun setUp() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            allowOverride(true)
            modules(dataModule)
        }

        loadKoinModules(
            module {
                single(named("DB_NAME")) { "test_kairos.db" }
            },
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `database file is created with overridden name`() {
        val db: AppDatabase = getKoin().get()
        val app: Application = ApplicationProvider.getApplicationContext()

        db.openHelper.writableDatabase

        val expected = app.getDatabasePath("test_kairos.db")
        assertTrue(expected.exists(), "Expected ${expected.absolutePath} to exist")

        db.close()
    }
}
