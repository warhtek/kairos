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
package mobi.kairos.android.data.entity

import junit.framework.TestCase
import org.junit.Test

class TranslationBookEntityUnitTest {
    @Test
    fun `entity initialization works and supports copy, equals and component`() {
        val entity = TranslationBookEntity(
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
        )

        TestCase.assertEquals("1", entity.id)
        TestCase.assertEquals("1", entity.component1())

        // copy()
        val copy = entity.copy(id = "2")
        TestCase.assertEquals("2", copy.id)
        TestCase.assertFalse(entity == copy)

        // equals + hashCode
        val entitySame = TranslationBookEntity(
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
        )
        TestCase.assertEquals(entity, entitySame)
        TestCase.assertEquals(entity.hashCode(), entitySame.hashCode())

        // toString()
        val text = entity.toString()
        assert(text.contains("TranslationBookEntity"))
        assert(text.contains("id=1"))
    }
}
