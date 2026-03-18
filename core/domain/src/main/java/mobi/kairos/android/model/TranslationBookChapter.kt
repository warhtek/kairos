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
package mobi.kairos.android.model

import kotlinx.serialization.Serializable

interface TranslationBookChapter {
    val translation: Translation
    val book: TranslationBook
    val thisChapterLink: String
    val thisChapterAudioLinks: Map<String, String>
    val nextChapterApiLink: String?
    val nextChapterAudioLinks: Map<String, String>?
    val previousChapterApiLink: String?
    val previousChapterAudioLinks: Map<String, String>?
    val numberOfVerses: Int
    val chapter: ChapterData
}

interface ChapterData {
    val number: Int
    val content: List<ChapterContent>
    val footnotes: List<ChapterFootnote>
}

@Serializable
interface ChapterContent

@Serializable
interface ChapterInlineContent {
    fun toText(): String = ""
}

interface ChapterHeading : ChapterContent {
    val type: String
    val content: List<String>
}

interface ChapterLineBreak : ChapterContent {
    val type: String
}

interface ChapterHebrewSubtitle : ChapterContent {
    val type: String
    val content: List<ChapterInlineContent>
}

interface ChapterVerse : ChapterContent {
    val type: String
    val number: Int
    val content: List<ChapterInlineContent>
}

interface ChapterFootnote {
    val noteId: Int
    val text: String
    val reference: ChapterFootnoteReference?
    val caller: String?
}

interface ChapterFootnoteReference {
    val chapter: Int
    val verse: Int
}
