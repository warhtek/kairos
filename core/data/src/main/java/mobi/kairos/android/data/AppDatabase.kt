package mobi.kairos.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mobi.kairos.android.data.dao.ChapterDao
import mobi.kairos.android.data.dao.DatabaseInfoDao
import mobi.kairos.android.data.dao.ReadingProgressDao
import mobi.kairos.android.data.dao.TranslationBookDao
import mobi.kairos.android.data.dao.TranslationDao
import mobi.kairos.android.data.entity.ReadingProgressEntity
import mobi.kairos.android.data.entity.TranslationBookChapterEntity
import mobi.kairos.android.data.entity.TranslationBookEntity
import mobi.kairos.android.data.entity.TranslationEntity

@Database(
    entities = [
        TranslationEntity::class,
        TranslationBookEntity::class,
        TranslationBookChapterEntity::class,
        ReadingProgressEntity::class,
    ],
    exportSchema = false,
    version = 2,
)
@androidx.room.TypeConverters(
    mobi.kairos.android.data.converter.TranslationBookChapterTypeConverters::class,
    mobi.kairos.android.data.converter.TranslationTypeConverters::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseInfoDao(): DatabaseInfoDao
    abstract fun translationDao(): TranslationDao
    abstract fun translationBookDao(): TranslationBookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readingProgressDao(): ReadingProgressDao
}

internal fun databaseBuilder(context: Context, dbName: String, notifier: RoomReadyNotifier): AppDatabase {
    return Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = dbName,
    )
        .fallbackToDestructiveMigration(true)
        .addCallback(notifier)
        .build()
}
