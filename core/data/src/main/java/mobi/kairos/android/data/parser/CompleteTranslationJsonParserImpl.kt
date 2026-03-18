@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package mobi.kairos.android.data.parser

import java.io.InputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import mobi.kairos.android.data.model.ChapterHeadingModel
import mobi.kairos.android.data.model.ChapterHebrewSubtitleModel
import mobi.kairos.android.data.model.ChapterLineBreakModel
import mobi.kairos.android.data.model.ChapterVerseModel
import mobi.kairos.android.data.model.CompleteBookModel
import mobi.kairos.android.data.model.CompleteTranslationResponse
import mobi.kairos.android.data.model.FormattedTextModel
import mobi.kairos.android.data.model.InlineHeadingModel
import mobi.kairos.android.data.model.InlineLineBreakModel
import mobi.kairos.android.data.model.TranslationBookChapterModel
import mobi.kairos.android.data.model.VerseFootnoteReferenceModel
import mobi.kairos.android.model.ChapterContent
import mobi.kairos.android.model.ChapterInlineContent
import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.model.TranslationBookChapter
import mobi.kairos.android.parser.CompleteTranslationJsonParser

class CompleteTranslationJsonParserImpl : CompleteTranslationJsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
        serializersModule = SerializersModule {
            polymorphic(ChapterContent::class) {
                subclass(ChapterHeadingModel::class)
                subclass(ChapterLineBreakModel::class)
                subclass(ChapterHebrewSubtitleModel::class)
                subclass(ChapterVerseModel::class)
            }
            polymorphic(ChapterInlineContent::class) {
                subclass(FormattedTextModel::class)
                subclass(InlineHeadingModel::class)
                subclass(InlineLineBreakModel::class)
                subclass(VerseFootnoteReferenceModel::class)
            }
        }
        classDiscriminator = "type"
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun parse(
        inputStream: InputStream,
        translationId: String,
    ): Result<List<TranslationBookChapter>> = runCatching {
        inputStream.use { stream ->
            val response = json.decodeFromStream<CompleteTranslationResponse>(stream)
            response.books.flatMap { book ->
                book.chapters.mapIndexed { index, chapter ->
                    TranslationBookChapterModel(
                        translation = response.translation,
                        book = book.toTranslationBook(translationId),
                        thisChapterLink = "/api/$translationId/${book.id}/${index + 1}.json",
                        thisChapterAudioLinks = chapter.thisChapterAudioLinks,
                        nextChapterApiLink = nextLink(translationId, book, index),
                        nextChapterAudioLinks = null,
                        previousChapterApiLink = previousLink(translationId, book, index),
                        previousChapterAudioLinks = null,
                        numberOfVerses = chapter.numberOfVerses,
                        chapter = chapter.chapter,
                    )
                }
            }
        }
    }

    private fun nextLink(
        translationId: String,
        book: CompleteBookModel,
        index: Int,
    ): String? = if (index + 1 < book.chapters.size) {
        "/api/$translationId/${book.id}/${index + 2}.json"
    } else null

    private fun previousLink(
        translationId: String,
        book: CompleteBookModel,
        index: Int,
    ): String? = if (index > 0) {
        "/api/$translationId/${book.id}/$index.json"
    } else null
}

private fun CompleteBookModel.toTranslationBook(translationId: String): TranslationBook =
    object : TranslationBook {
        override val id = this@toTranslationBook.id
        override val name = this@toTranslationBook.name
        override val commonName = this@toTranslationBook.commonName
        override val title = this@toTranslationBook.title
        override val order = this@toTranslationBook.order
        override val numberOfChapters = this@toTranslationBook.numberOfChapters
        override val firstChapterNumber = 1
        override val firstChapterApiLink = "/api/$translationId/${this@toTranslationBook.id}/1.json"
        override val lastChapterNumber = this@toTranslationBook.numberOfChapters
        override val lastChapterApiLink = "/api/$translationId/${this@toTranslationBook.id}/${this@toTranslationBook.numberOfChapters}.json"
        override val totalNumberOfVerses = this@toTranslationBook.totalNumberOfVerses
        override val isApocryphal = false
    }
