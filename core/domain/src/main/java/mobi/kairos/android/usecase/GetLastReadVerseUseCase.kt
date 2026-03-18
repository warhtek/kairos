package mobi.kairos.android.usecase

import mobi.kairos.android.repository.ReadingProgressRepository

class GetLastReadVerseUseCase(private val repository: ReadingProgressRepository) {
    suspend operator fun invoke(): Result<LastReadVerse?> = repository.getLastRead()
}

data class LastReadVerse(
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val verseText: String,
)
