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
package mobi.kairos.android.data.converter

import kotlin.test.assertEquals
import org.junit.Test
import mobi.kairos.android.data.model.ChapterFootnoteModel
import mobi.kairos.android.data.model.ChapterHeadingModel
import mobi.kairos.android.data.model.ChapterLineBreakModel
import mobi.kairos.android.data.model.ChapterVerseModel
import mobi.kairos.android.data.model.FormattedTextModel
import mobi.kairos.android.data.model.VerseFootnoteReferenceModel

class TranslationBookChapterTypeConvertersTest {

    private val converters = TranslationBookChapterTypeConverters()

    @Test
    fun `fromChapterContentList - should convert heading to JSON string`() {
        val content = listOf(ChapterHeadingModel(content = listOf("In the beginning")))
        val result = converters.fromChapterContentList(content)
        assert(result.contains("heading"))
        assert(result.contains("In the beginning"))
    }

    @Test
    fun `toChapterContentList - should convert JSON string to list`() {
        val content = listOf(ChapterHeadingModel(content = listOf("In the beginning")))
        val json = converters.fromChapterContentList(content)
        val result = converters.toChapterContentList(json)
        assertEquals(content, result)
    }

    @Test
    fun `fromChapterContentList - should handle line break`() {
        val content = listOf(ChapterLineBreakModel)
        val result = converters.fromChapterContentList(content)
        assert(result.contains("line_break"))
    }

    @Test
    fun `fromChapterContentList - should handle verse with inline content`() {
        val content = listOf(
            ChapterVerseModel(
                number = 1,
                content = listOf(
                    FormattedTextModel(text = "In the beginning God created"),
                    VerseFootnoteReferenceModel(noteId = 1),
                ),
            ),
        )
        val json = converters.fromChapterContentList(content)
        val result = converters.toChapterContentList(json)
        assertEquals(content, result)
    }

    @Test
    fun `fromChapterFootnoteList - should convert footnotes to JSON string`() {
        val footnotes = listOf(ChapterFootnoteModel(noteId = 1, text = "Some footnote", caller = "+"))
        val result = converters.fromChapterFootnoteList(footnotes)
        assert(result.contains("Some footnote"))
    }

    @Test
    fun `toChapterFootnoteList - should convert JSON string back to footnotes`() {
        val footnotes = listOf(ChapterFootnoteModel(noteId = 1, text = "Some footnote", caller = "+"))
        val json = converters.fromChapterFootnoteList(footnotes)
        val result = converters.toChapterFootnoteList(json)
        assertEquals(footnotes, result)
    }

    @Test
    fun `fromAudioLinks - should convert map to JSON string`() {
        val links = mapOf("reader1" to "https://audio.example.com/1.mp3")
        val result = converters.fromAudioLinks(links)
        assert(result.contains("reader1"))
        assert(result.contains("https://audio.example.com/1.mp3"))
    }

    @Test
    fun `toAudioLinks - should convert JSON string back to map`() {
        val links = mapOf("reader1" to "https://audio.example.com/1.mp3")
        val json = converters.fromAudioLinks(links)
        val result = converters.toAudioLinks(json)
        assertEquals(links, result)
    }

    @Test
    fun `fromAudioLinks - empty map returns valid JSON`() {
        val result = converters.fromAudioLinks(emptyMap())
        val roundTrip = converters.toAudioLinks(result)
        assertEquals(emptyMap<String, String>(), roundTrip)
    }
}
