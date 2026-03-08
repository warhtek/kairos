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
package mobi.kairos.android.data.dao

import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import mobi.kairos.android.data.BaseKoinTest

@RunWith(RobolectricTestRunner::class)
class TranslationBookDaoTest : BaseKoinTest() {
    @Test
    fun `count should return 0`() = runTest {
        // Given
        val translationBookDao = getKoin().get<TranslationBookDao>()

        // When
        val count = translationBookDao.count()

        // Then
        TestCase.assertEquals(0, count)
    }
}
