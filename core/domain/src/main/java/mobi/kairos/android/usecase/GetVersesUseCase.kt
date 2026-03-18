package mobi.kairos.android.usecase

import mobi.kairos.android.model.ChapterVerse
import mobi.kairos.android.repository.ChapterRepository

class GetVersesUseCase(private val repository: ChapterRepository) {
    suspend operator fun invoke(
        translationId: String,
        bookId: String,
        chapterNumber: Int,
    ): Result<List<ChapterVerse>> = repository.getChapter(
        translationId = translationId,
        bookId = bookId,
        chapterNumber = chapterNumber,
    ).map { chapter ->
        chapter.chapter.content.filterIsInstance<ChapterVerse>()
    }
}
