package mobi.kairos.android.usecase

import mobi.kairos.android.parser.CompleteTranslationJsonParser
import mobi.kairos.android.repository.ChapterRepository
import mobi.kairos.android.resource.CompleteTranslationAsset

class ImportChaptersUseCase(
    private val completeTranslationAsset: CompleteTranslationAsset,
    private val jsonParser: CompleteTranslationJsonParser,
    private val chapterRepository: ChapterRepository,
) {
    suspend operator fun invoke(translationId: String): Result<ImportSummary> {
        val startTime = System.currentTimeMillis()

        return try {
            val stream = completeTranslationAsset
                .openJsonStream(translationId)
                .getOrElse { return Result.failure(it) }

            val chapters = jsonParser
                .parse(stream, translationId)
                .getOrElse { return Result.failure(it) }

            chapterRepository.importChapters(chapters)

            Result.success(
                ImportSummary(
                    success = true,
                    count = chapters.size,
                    durationMs = System.currentTimeMillis() - startTime,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
