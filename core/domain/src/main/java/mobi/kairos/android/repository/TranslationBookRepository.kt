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
package mobi.kairos.android.repository

import mobi.kairos.android.model.TranslationBook

interface TranslationBookRepository {
    suspend fun importBooks(books: List<TranslationBook>)
    suspend fun count(): Int
    suspend fun getBooks(): List<TranslationBook>
    suspend fun getBookById(bookId: String): TranslationBook?
    suspend fun getNextBook(bookId: String): TranslationBook?
    suspend fun getPreviousBook(bookId: String): TranslationBook?
}
