package mobi.kairos.android.usecase

import mobi.kairos.android.model.TranslationBookChapter
import mobi.kairos.android.repository.ChapterRepository

class GetChapterUseCase(private val repository: ChapterRepository) {
    suspend operator fun invoke(
        translationId: String,
        bookId: String,
        chapterNumber: Int,
    ): Result<TranslationBookChapter> = repository.getChapter(translationId, bookId, chapterNumber)
}
