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

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import mobi.kairos.android.data.converter.TranslationBookChapterTypeConverters
import mobi.kairos.android.data.converter.TranslationTypeConverters
import mobi.kairos.android.data.model.ChapterFootnoteModel
import mobi.kairos.android.model.ChapterContent
import mobi.kairos.android.model.TranslationBookChapter

@Entity(tableName = "translation_book_chapters")
@TypeConverters(
    TranslationBookChapterTypeConverters::class,
    TranslationTypeConverters::class,
)
data class TranslationBookChapterEntity(
    @PrimaryKey
    val thisChapterLink: String,
    val translationId: String,
    val bookId: String,
    val nextChapterApiLink: String?,
    val previousChapterApiLink: String?,
    val numberOfVerses: Int,
    val thisChapterAudioLinks: String,
    val nextChapterAudioLinks: String?,
    val previousChapterAudioLinks: String?,
    val chapterNumber: Int,
    val chapterContent: String,
    val chapterFootnotes: String,
)

fun TranslationBookChapter.toEntity(): TranslationBookChapterEntity {
    val converters = TranslationBookChapterTypeConverters()
    return TranslationBookChapterEntity(
        thisChapterLink = thisChapterLink,
        translationId = translation.id,
        bookId = book.id,
        nextChapterApiLink = nextChapterApiLink,
        previousChapterApiLink = previousChapterApiLink,
        numberOfVerses = numberOfVerses,
        thisChapterAudioLinks = converters.fromAudioLinks(thisChapterAudioLinks),
        nextChapterAudioLinks = nextChapterAudioLinks?.let { converters.fromAudioLinks(it) },
        previousChapterAudioLinks = previousChapterAudioLinks?.let { converters.fromAudioLinks(it) },
        chapterNumber = chapter.number,
        chapterContent = converters.fromChapterContentList(chapter.content),
        chapterFootnotes = converters.fromChapterFootnoteList(chapter.footnotes),
    )
}
