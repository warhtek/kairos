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
package mobi.kairos.android.usecase

import mobi.kairos.android.parser.TranslationJsonParser
import mobi.kairos.android.repository.TranslationRepository
import mobi.kairos.android.resource.TranslationsAsset

class ImportTranslationsUseCase(
    private val translationsAsset: TranslationsAsset,
    private val jsonParser: TranslationJsonParser,
    private val translationRepository: TranslationRepository,
) {
    suspend operator fun invoke(): Result<ImportSummary> {
        val startTime = System.currentTimeMillis()

        return try {
            val stream = translationsAsset.openJsonStream().getOrElse { return Result.failure(it) }
            val translations = jsonParser.parse(stream).getOrElse { return Result.failure(it) }

            translationRepository.importTranslations(translations)

            Result.success(
                ImportSummary(
                    success = true,
                    count = translations.size,
                    durationMs = System.currentTimeMillis() - startTime,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ImportSummary(val success: Boolean, val count: Int, val durationMs: Long)
