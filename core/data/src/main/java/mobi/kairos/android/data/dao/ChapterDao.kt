package mobi.kairos.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mobi.kairos.android.data.entity.TranslationBookChapterEntity

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<TranslationBookChapterEntity>)

    @Query("SELECT * FROM translation_book_chapters WHERE translationId = :translationId AND bookId = :bookId AND chapterNumber = :chapterNumber LIMIT 1")
    suspend fun getChapter(
        translationId: String,
        bookId: String,
        chapterNumber: Int,
    ): TranslationBookChapterEntity?

    @Query("SELECT COUNT(*) FROM translation_book_chapters")
    suspend fun count(): Int
}
