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
package mobi.kairos.android.data.repository

import mobi.kairos.android.data.dao.TranslationDao
import mobi.kairos.android.data.entity.toEntity
import mobi.kairos.android.model.Translation
import mobi.kairos.android.repository.TranslationRepository

class TranslationRepositoryImpl(private val translationDao: TranslationDao) : TranslationRepository {
    override suspend fun importTranslations(translations: List<Translation>) {
        translationDao.insertAll(translations.map { it.toEntity() })
    }
}
