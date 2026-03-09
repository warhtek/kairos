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

import mobi.kairos.android.parser.TranslationBookJsonParser
import mobi.kairos.android.repository.TranslationBookRepository
import mobi.kairos.android.resource.TranslationBooksAsset

class ImportTranslationBooksUseCase(
    private val translationBooksAsset: TranslationBooksAsset,
    private val jsonParser: TranslationBookJsonParser,
    private val translationBookRepository: TranslationBookRepository,
) {
    suspend operator fun invoke(translations: List<String>): Result<ImportSummary> {
        val startTime = System.currentTimeMillis()

        return try {
            var totalBooks = 0
            translations.forEach {
                val stream = translationBooksAsset.openJsonStream(it).getOrElse { return Result.failure(it) }
                val books = jsonParser.parse(stream).getOrElse { return Result.failure(it) }

                translationBookRepository.importBooks(books)

                totalBooks += books.size
            }
            Result.success(
                ImportSummary(
                    success = true,
                    count = totalBooks,
                    durationMs = System.currentTimeMillis() - startTime,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
