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

import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertNotNull
import mobi.kairos.android.data.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataModuleVerifyTest : KoinTest {
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(dataModule)
        }
        db = KoinJavaComponent.getKoin().get()
    }

    @After
    fun tearDown() {
        stopKoin()
        db.close()
    }

    @Test @KoinExperimentalAPI
    fun `verify all declared class constructors are bound`() {
        dataModule.verify()
    }

    @Test
    fun `can resolve AppDatabase`() {
        assertNotNull(db, "AppDatabase should be resolved from Koin")
    }
}
