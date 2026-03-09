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
import kotlinx.serialization.InternalSerializationApi
import org.junit.Test
import mobi.kairos.android.data.model.TranslationBookModel

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
    @OptIn(InternalSerializationApi::class)
    @Test
    fun `toEntity should map TranslationBook to TranslationBookEntity correctly`() {
        val book = TranslationBookModel(
            id = "GEN",
            name = "Genesis",
            commonName = "Genesis",
            title = "The Book of Genesis",
            order = 1,
            numberOfChapters = 50,
            firstChapterNumber = 1,
            firstChapterApiLink = "/api/spa_r09/GEN/1.json",
            lastChapterNumber = 50,
            lastChapterApiLink = "/api/spa_r09/GEN/50.json",
            totalNumberOfVerses = 1533,
            isApocryphal = false,
        )

        val entity = book.toEntity()

        TestCase.assertEquals("GEN", entity.id)
        TestCase.assertEquals("Genesis", entity.name)
        TestCase.assertEquals("Genesis", entity.commonName)
        TestCase.assertEquals("The Book of Genesis", entity.title)
        TestCase.assertEquals(1, entity.order)
        TestCase.assertEquals(50, entity.numberOfChapters)
        TestCase.assertEquals(1, entity.firstChapterNumber)
        TestCase.assertEquals("/api/spa_r09/GEN/1.json", entity.firstChapterApiLink)
        TestCase.assertEquals(50, entity.lastChapterNumber)
        TestCase.assertEquals("/api/spa_r09/GEN/50.json", entity.lastChapterApiLink)
        TestCase.assertEquals(1533, entity.totalNumberOfVerses)
        TestCase.assertFalse(entity.isApocryphal)
    }
}
