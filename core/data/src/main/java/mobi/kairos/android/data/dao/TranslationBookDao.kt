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
package mobi.kairos.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mobi.kairos.android.data.entity.TranslationBookEntity

@Dao
interface TranslationBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<TranslationBookEntity>)

    @Query("SELECT COUNT(*) FROM translation_books")
    suspend fun count(): Int

    @Query("SELECT * FROM translation_books ORDER BY `order` ASC")
    suspend fun getAll(): List<TranslationBookEntity>
}
