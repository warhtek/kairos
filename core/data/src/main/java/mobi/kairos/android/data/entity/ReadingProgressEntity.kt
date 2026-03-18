package mobi.kairos.android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val verseText: String,
    val savedAt: Long = System.currentTimeMillis(),
)
