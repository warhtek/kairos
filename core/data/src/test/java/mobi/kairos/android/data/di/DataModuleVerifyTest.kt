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

import android.util.Log
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import org.robolectric.RobolectricTestRunner
import mobi.kairos.android.data.BaseKoinTest

@RunWith(RobolectricTestRunner::class)
class DataModuleVerifyTest : BaseKoinTest() {
    @Test
    @KoinExperimentalAPI
    fun `verify all declared class constructors are bound`() = runTest {
        runCatching {
            dataModule.verify()
        }.onFailure {
            Log.e("DataModuleVerifyTest", "Error detected during Koin verification", it)
        }
    }
}
