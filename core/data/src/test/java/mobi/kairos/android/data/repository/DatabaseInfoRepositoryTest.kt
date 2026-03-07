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
package mobi.kairos.android.data.repository

import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import mobi.kairos.android.data.di.dataModule
import mobi.kairos.android.repository.DatabaseRepository

@RunWith(RobolectricTestRunner::class)
class DatabaseInfoRepositoryTest : KoinTest {
    private lateinit var databaseRepository: DatabaseRepository

    @Before
    fun setUp() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(dataModule)
        }
        databaseRepository = getKoin().get()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `getVersion returns 1`() = runTest {
        val version = databaseRepository.getVersion()
        assertEquals(1, version)
    }
}
