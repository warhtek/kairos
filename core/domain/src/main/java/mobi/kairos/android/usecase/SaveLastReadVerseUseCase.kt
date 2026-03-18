package mobi.kairos.android.usecase

import mobi.kairos.android.repository.ReadingProgressRepository

class SaveLastReadVerseUseCase(private val repository: ReadingProgressRepository) {
    suspend operator fun invoke(verse: LastReadVerse) = repository.saveLastRead(verse)
}
