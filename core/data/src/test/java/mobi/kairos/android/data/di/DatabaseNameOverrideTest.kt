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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseNameOverrideTest : KoinTest {
    private lateinit var database: AppDatabase

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

        database = get()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
        stopKoin()
    }

    @Test
    fun `database file is created with overridden name`() {
        val app: Application = ApplicationProvider.getApplicationContext()
        database.openHelper.writableDatabase.use {}

        val expected = app.getDatabasePath("test_kairos.db")
        assertTrue(expected.exists(), "Expected ${expected.absolutePath} to exist")
        assertTrue(expected.length() > 0, "Database file should not be empty")
    }

    @Test
    fun `database instance is created with correct name`() {
        database.openHelper.writableDatabase.use { db ->
            assertNotNull(db)
            assertEquals(db.path?.endsWith("test_kairos.db"), true, "Database path should end with test_kairos.db but was: ${db.path}")
        }
    }
}
