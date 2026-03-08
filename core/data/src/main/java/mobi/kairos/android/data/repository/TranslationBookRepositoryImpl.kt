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

import mobi.kairos.android.data.dao.TranslationBookDao
import mobi.kairos.android.data.entity.toEntity
import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.repository.TranslationBookRepository

class TranslationBookRepositoryImpl(private val translationBookDao: TranslationBookDao) : TranslationBookRepository {
    override suspend fun importBooks(books: List<TranslationBook>) = translationBookDao.insertAll(books.map { it.toEntity() })
    override suspend fun count() = translationBookDao.count()
}
