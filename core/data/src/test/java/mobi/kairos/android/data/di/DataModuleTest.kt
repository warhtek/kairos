/**
 * Copyright (C) 2026 MOBIWARE
 *
 * This software and its source code are the exclusive property of MOBIWARE.
 *
 * Any unauthorized use, copying, distribution, or modification of this software, in whole or in part,
 * may result in severe civil and criminal penalties under applicable copyright and trade secret laws.
 */
package mobi.kairos.android.data.di

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class DataModuleTest : KoinTest {
    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            modules(dataModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `verify all declared class constructors are bound`() {
        dataModule.verify()
    }
}