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
import mobi.kairos.android.data.model.ChapterFootnoteModel
import mobi.kairos.android.model.ChapterContent
import mobi.kairos.android.model.TranslationBookChapter

@Entity(tableName = "translation_book_chapters")
@TypeConverters(TranslationBookChapterTypeConverters::class)
data class TranslationBookChapterEntity(
    @PrimaryKey
    val thisChapterLink: String,
    val translationId: String,
    val bookId: String,
    val nextChapterApiLink: String?,
    val previousChapterApiLink: String?,
    val numberOfVerses: Int,
    val thisChapterAudioLinks: Map<String, String>,
    val nextChapterAudioLinks: Map<String, String>?,
    val previousChapterAudioLinks: Map<String, String>?,
    val chapterNumber: Int,
    val chapterContent: List<ChapterContent>,
    val chapterFootnotes: List<ChapterFootnoteModel>,
)

fun TranslationBookChapter.toEntity(): TranslationBookChapterEntity = TranslationBookChapterEntity(
    thisChapterLink = thisChapterLink,
    translationId = translation.id,
    bookId = book.id,
    nextChapterApiLink = nextChapterApiLink,
    previousChapterApiLink = previousChapterApiLink,
    numberOfVerses = numberOfVerses,
    thisChapterAudioLinks = thisChapterAudioLinks,
    nextChapterAudioLinks = nextChapterAudioLinks,
    previousChapterAudioLinks = previousChapterAudioLinks,
    chapterNumber = chapter.number,
    chapterContent = chapter.content,
    chapterFootnotes = chapter.footnotes.filterIsInstance<ChapterFootnoteModel>(),
)
