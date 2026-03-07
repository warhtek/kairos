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

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Test
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection

class TranslationEntityUnitTest {
    @Test
    fun `entity initialization works and supports copy, equals and component`() {
        val entity = TranslationEntity(
            id = "5L",
            name = "name",
            englishName = "englishName",
            website = "website",
            licenseUrl = "licenseUrl",
            shortName = "shortName",
            language = "language",
            languageName = "languageName",
            languageEnglishName = "languageEnglishName",
            textDirection = TextDirection.LTR,
            availableFormats = listOf(AvailableFormat.JSON),
            listOfBooksApiLink = "listOfBooksApiLink",
            numberOfBooks = 1,
            totalNumberOfChapters = 2,
            totalNumberOfVerses = 3,
            numberOfApocryphalBooks = 4,
            totalNumberOfApocryphalChapters = 5,
            totalNumberOfApocryphalVerses = 6,
        )

        assertEquals("5L", entity.id)
        assertEquals("5L", entity.component1())

        // copy()
        val copy = entity.copy(id = "10L")
        assertEquals("10L", copy.id)
        assertNotEquals(entity, copy)

        // equals + hashCode
        val entitySame = TranslationEntity(
            id = "5L",
            name = "name",
            englishName = "englishName",
            website = "website",
            licenseUrl = "licenseUrl",
            shortName = "shortName",
            language = "language",
            languageName = "languageName",
            languageEnglishName = "languageEnglishName",
            textDirection = TextDirection.LTR,
            availableFormats = listOf(AvailableFormat.JSON),
            listOfBooksApiLink = "listOfBooksApiLink",
            numberOfBooks = 1,
            totalNumberOfChapters = 2,
            totalNumberOfVerses = 3,
            numberOfApocryphalBooks = 4,
            totalNumberOfApocryphalChapters = 5,
            totalNumberOfApocryphalVerses = 6,
        )
        assertEquals(entity, entitySame)
        assertEquals(entity.hashCode(), entitySame.hashCode())

        // toString()
        val text = entity.toString()
        assert(text.contains("TranslationEntity"))
        assert(text.contains("id=5L"))
    }
}
