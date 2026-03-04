package mobi.kairos.android.usecase

import mobi.kairos.android.repository.DatabaseRepository

class GetDatabaseVersionUseCase(
    private val repository: DatabaseRepository
) {
    suspend operator fun invoke(): Int = repository.getVersion()
}
