package mobi.kairos.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mobi.kairos.android.data.entity.ReadingProgressEntity

@Dao
interface ReadingProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(progress: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress ORDER BY savedAt DESC LIMIT 1")
    suspend fun getLastRead(): ReadingProgressEntity?
}
