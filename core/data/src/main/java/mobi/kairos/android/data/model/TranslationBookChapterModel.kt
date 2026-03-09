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
package mobi.kairos.android.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mobi.kairos.android.model.ChapterContent
import mobi.kairos.android.model.ChapterData
import mobi.kairos.android.model.ChapterFootnote
import mobi.kairos.android.model.ChapterFootnoteReference
import mobi.kairos.android.model.ChapterHeading
import mobi.kairos.android.model.ChapterHebrewSubtitle
import mobi.kairos.android.model.ChapterInlineContent
import mobi.kairos.android.model.ChapterLineBreak
import mobi.kairos.android.model.ChapterVerse
import mobi.kairos.android.model.Translation
import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.model.TranslationBookChapter

data class TranslationBookChapterModel(
    override val translation: Translation,
    override val book: TranslationBook,
    override val thisChapterLink: String,
    override val thisChapterAudioLinks: Map<String, String>,
    override val nextChapterApiLink: String?,
    override val nextChapterAudioLinks: Map<String, String>?,
    override val previousChapterApiLink: String?,
    override val previousChapterAudioLinks: Map<String, String>?,
    override val numberOfVerses: Int,
    override val chapter: ChapterData,
) : TranslationBookChapter

data class ChapterDataModel(
    override val number: Int,
    override val content: List<ChapterContent>,
    override val footnotes: List<ChapterFootnote>,
) : ChapterData

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("heading")
data class ChapterHeadingModel(override val content: List<String>) :
    ChapterHeading,
    ChapterContent {
    override val type: String get() = "heading"
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("line_break")
object ChapterLineBreakModel : ChapterLineBreak, ChapterContent {
    override val type: String get() = "line_break"
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("hebrew_subtitle")
data class ChapterHebrewSubtitleModel(override val content: List<ChapterInlineContent>) :
    ChapterHebrewSubtitle,
    ChapterContent {
    override val type: String get() = "hebrew_subtitle"
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("verse")
data class ChapterVerseModel(override val number: Int, override val content: List<ChapterInlineContent>) :
    ChapterVerse,
    ChapterContent {
    override val type: String get() = "verse"
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("text")
data class FormattedTextModel(val text: String, val poem: Int? = null, val wordsOfJesus: Boolean? = null) : ChapterInlineContent

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("inline_heading")
data class InlineHeadingModel(val heading: String) : ChapterInlineContent

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("inline_line_break")
data class InlineLineBreakModel(val lineBreak: Boolean = true) : ChapterInlineContent

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("footnote_reference")
data class VerseFootnoteReferenceModel(val noteId: Int) : ChapterInlineContent

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChapterFootnoteModel(
    override val noteId: Int,
    override val text: String,
    override val reference: ChapterFootnoteReferenceModel? = null,
    override val caller: String? = null,
) : ChapterFootnote

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChapterFootnoteReferenceModel(override val chapter: Int, override val verse: Int) : ChapterFootnoteReference
