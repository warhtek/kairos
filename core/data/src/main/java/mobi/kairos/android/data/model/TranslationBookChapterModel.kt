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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

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

@SuppressLint("UnsafeOptInUsageError")
@Serializable
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
data class ChapterVerseModel(
    override val number: Int,
    @SerialName("content")
    val contentRaw: JsonArray = JsonArray(emptyList()),
) : ChapterVerse, ChapterContent {
    override val type: String get() = "verse"
    override val content: List<ChapterInlineContent>
        get() = contentRaw.map { element ->
            when {
                element is JsonPrimitive -> FormattedTextModel(element.content)
                element is JsonObject && element["type"]?.jsonPrimitive?.content == "footnote_reference" ->
                    VerseFootnoteReferenceModel(element["noteId"]?.jsonPrimitive?.int ?: 0)
                element is JsonObject && element["type"]?.jsonPrimitive?.content == "inline_heading" ->
                    InlineHeadingModel(element["heading"]?.jsonPrimitive?.content ?: "")
                else -> FormattedTextModel(element.toString())
            }
        }
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("text")
data class FormattedTextModel(
    val text: String,
    val poem: Int? = null,
    val wordsOfJesus: Boolean? = null,
) : ChapterInlineContent {
    override fun toText(): String = text
}

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
