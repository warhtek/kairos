package mobi.kairos.android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "k_content")
data class ContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
)
