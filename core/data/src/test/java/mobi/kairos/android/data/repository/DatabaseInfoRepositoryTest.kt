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

import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import mobi.kairos.android.data.BaseKoinTest
import mobi.kairos.android.repository.DatabaseRepository

@RunWith(RobolectricTestRunner::class)
class DatabaseInfoRepositoryTest : BaseKoinTest() {
    @Test
    fun `getVersion returns 1`() = runTest {
        // Given
        val databaseInfoRepository = getKoin().get<DatabaseRepository>()

        // Then
        assertEquals(1, databaseInfoRepository.getVersion())
    }
}
