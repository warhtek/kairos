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
import mobi.kairos.android.data.model.ChapterDataModel
import mobi.kairos.android.data.model.ChapterFootnoteModel
import mobi.kairos.android.data.model.ChapterHeadingModel
import mobi.kairos.android.data.model.TranslationBookChapterModel
import mobi.kairos.android.data.model.TranslationBookModel
import mobi.kairos.android.data.model.TranslationImportModel
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection

class TranslationBookChapterEntityUnitTest {
    @Test
    fun `entity initialization works and supports copy, equals and component`() {
        val entity = TranslationBookChapterEntity(
            thisChapterLink = "/api/spa_r09/GEN/1.json",
            translationId = "spa_r09",
            bookId = "GEN",
            nextChapterApiLink = "/api/spa_r09/GEN/2.json",
            previousChapterApiLink = null,
            numberOfVerses = 31,
            thisChapterAudioLinks = mapOf("reader1" to "https://audio.example.com/1.mp3"),
            nextChapterAudioLinks = mapOf("reader1" to "https://audio.example.com/2.mp3"),
            previousChapterAudioLinks = null,
            chapterNumber = 1,
            chapterContent = listOf(ChapterHeadingModel(content = listOf("In the beginning"))),
            chapterFootnotes = listOf(ChapterFootnoteModel(noteId = 1, text = "Some footnote", caller = "+")),
        )

        TestCase.assertEquals("/api/spa_r09/GEN/1.json", entity.thisChapterLink)
        TestCase.assertEquals("/api/spa_r09/GEN/1.json", entity.component1())

        // copy()
        val copy = entity.copy(thisChapterLink = "/api/spa_r09/GEN/2.json")
        TestCase.assertEquals("/api/spa_r09/GEN/2.json", copy.thisChapterLink)
        TestCase.assertFalse(entity == copy)

        // equals + hashCode
        val entitySame = TranslationBookChapterEntity(
            thisChapterLink = "/api/spa_r09/GEN/1.json",
            translationId = "spa_r09",
            bookId = "GEN",
            nextChapterApiLink = "/api/spa_r09/GEN/2.json",
            previousChapterApiLink = null,
            numberOfVerses = 31,
            thisChapterAudioLinks = mapOf("reader1" to "https://audio.example.com/1.mp3"),
            nextChapterAudioLinks = mapOf("reader1" to "https://audio.example.com/2.mp3"),
            previousChapterAudioLinks = null,
            chapterNumber = 1,
            chapterContent = listOf(ChapterHeadingModel(content = listOf("In the beginning"))),
            chapterFootnotes = listOf(ChapterFootnoteModel(noteId = 1, text = "Some footnote", caller = "+")),
        )
        TestCase.assertEquals(entity, entitySame)
        TestCase.assertEquals(entity.hashCode(), entitySame.hashCode())

        // toString()
        val text = entity.toString()
        assert(text.contains("TranslationBookChapterEntity"))
        assert(text.contains("thisChapterLink=/api/spa_r09/GEN/1.json"))
    }

    @OptIn(InternalSerializationApi::class)
    @Test
    fun `toEntity should map TranslationBookChapter to TranslationBookChapterEntity correctly`() {
        val footnote = ChapterFootnoteModel(noteId = 1, text = "Some footnote", caller = "+")
        val content = listOf(ChapterHeadingModel(content = listOf("In the beginning")))

        val chapter = TranslationBookChapterModel(
            translation = TranslationImportModel(
                id = "spa_r09",
                name = "Reina Valera",
                englishName = "Reina Valera",
                website = "",
                licenseUrl = "",
                shortName = "R09",
                language = "spa",
                languageName = null,
                languageEnglishName = null,
                textDirection = TextDirection.LTR,
                availableFormats = listOf(AvailableFormat.JSON),
                listOfBooksApiLink = "",
                numberOfBooks = 66,
                totalNumberOfChapters = 1189,
                totalNumberOfVerses = 31102,
                numberOfApocryphalBooks = null,
                totalNumberOfApocryphalChapters = null,
                totalNumberOfApocryphalVerses = null,
            ),
            book = TranslationBookModel(
                id = "GEN",
                name = "Genesis",
                commonName = "Genesis",
                title = null,
                order = 1,
                numberOfChapters = 50,
                firstChapterNumber = 1,
                firstChapterApiLink = "/api/spa_r09/GEN/1.json",
                lastChapterNumber = 50,
                lastChapterApiLink = "/api/spa_r09/GEN/50.json",
                totalNumberOfVerses = 1533,
                isApocryphal = false,
            ),
            thisChapterLink = "/api/spa_r09/GEN/1.json",
            thisChapterAudioLinks = mapOf("reader1" to "https://audio.example.com/1.mp3"),
            nextChapterApiLink = "/api/spa_r09/GEN/2.json",
            nextChapterAudioLinks = mapOf("reader1" to "https://audio.example.com/2.mp3"),
            previousChapterApiLink = null,
            previousChapterAudioLinks = null,
            numberOfVerses = 31,
            chapter = ChapterDataModel(
                number = 1,
                content = content,
                footnotes = listOf(footnote),
            ),
        )

        val entity = chapter.toEntity()

        TestCase.assertEquals("/api/spa_r09/GEN/1.json", entity.thisChapterLink)
        TestCase.assertEquals("spa_r09", entity.translationId)
        TestCase.assertEquals("GEN", entity.bookId)
        TestCase.assertEquals("/api/spa_r09/GEN/2.json", entity.nextChapterApiLink)
        TestCase.assertNull(entity.previousChapterApiLink)
        TestCase.assertEquals(31, entity.numberOfVerses)
        TestCase.assertEquals(1, entity.chapterNumber)
        TestCase.assertEquals(content, entity.chapterContent)
        TestCase.assertEquals(listOf(footnote), entity.chapterFootnotes)
    }
}
