package mobi.kairos.android.repository

import mobi.kairos.android.model.TranslationBookChapter

interface ChapterRepository {
    suspend fun getChapter(
        translationId: String,
        bookId: String,
        chapterNumber: Int,
    ): Result<TranslationBookChapter>

    suspend fun importChapters(chapters: List<TranslationBookChapter>)
    suspend fun count(): Int
}
