package mobi.kairos.android.usecase

import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.repository.TranslationBookRepository

class GetBooksUseCase(private val repository: TranslationBookRepository) {
    suspend operator fun invoke(): Result<List<TranslationBook>> =
        runCatching { repository.getBooks() }
}
