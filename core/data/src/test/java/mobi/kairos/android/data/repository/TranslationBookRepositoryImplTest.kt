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

import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import mobi.kairos.android.data.BaseKoinTest
import mobi.kairos.android.data.entity.TranslationBookEntity
import mobi.kairos.android.repository.TranslationBookRepository

@RunWith(RobolectricTestRunner::class)
class TranslationBookRepositoryImplTest : BaseKoinTest() {
    @Test
    fun `importBooks should call insertAll on dao with given books`() = runTest {
        // Given
        val translationBookRepository = getKoin().get<TranslationBookRepository>()
        val books = listOf(
            TranslationBookEntity(
                id = "1",
                name = "Genesis",
                commonName = "Genesis",
                title = "The Book of Genesis",
                order = 1,
                numberOfChapters = 50,
                firstChapterNumber = 1,
                firstChapterApiLink = "firstChapterApiLink",
                lastChapterNumber = 50,
                lastChapterApiLink = "lastChapterApiLink",
                totalNumberOfVerses = 1533,
                isApocryphal = false,
            ),
        )

        // When
        translationBookRepository.importBooks(books)
    }

    @Test
    fun `count should return the number of books in the database`() = runTest {
        // Given
        val translationBookRepository = getKoin().get<TranslationBookRepository>()
        val books = listOf(
            TranslationBookEntity(
                id = "1",
                name = "Genesis",
                commonName = "Genesis",
                title = "The Book of Genesis",
                order = 1,
                numberOfChapters = 50,
                firstChapterNumber = 1,
                firstChapterApiLink = "firstChapterApiLink",
                lastChapterNumber = 50,
                lastChapterApiLink = "lastChapterApiLink",
                totalNumberOfVerses = 1533,
                isApocryphal = false,
            ),
        )

        // When
        translationBookRepository.importBooks(books)
        val count = translationBookRepository.count()

        // Then
        TestCase.assertEquals(1, count)
    }
}
