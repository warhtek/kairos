package mobi.kairos.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import mobi.kairos.android.data.dao.ChapterDao
import mobi.kairos.android.data.dao.DatabaseInfoDao
import mobi.kairos.android.data.dao.ReadingProgressDao
import mobi.kairos.android.data.dao.TranslationBookDao
import mobi.kairos.android.data.dao.TranslationDao
import mobi.kairos.android.data.dao.FavoriteVerseDao
import mobi.kairos.android.data.dao.DownloadedTranslationDao
import mobi.kairos.android.data.entity.ReadingProgressEntity
import mobi.kairos.android.data.entity.TranslationBookChapterEntity
import mobi.kairos.android.data.entity.TranslationBookEntity
import mobi.kairos.android.data.entity.TranslationEntity
import mobi.kairos.android.data.entity.FavoriteVerseEntity
import mobi.kairos.android.data.entity.DownloadedTranslationEntity

// Migración de versión 2 a 3 (agrega tabla de favoritos)
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `favorite_verses` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `bookId` TEXT NOT NULL,
                `bookName` TEXT NOT NULL,
                `chapterNumber` INTEGER NOT NULL,
                `verseNumber` INTEGER NOT NULL,
                `verseText` TEXT NOT NULL,
                `translationId` TEXT NOT NULL,
                `dateAdded` INTEGER NOT NULL
            )
        """)
    }
}

// Migración de versión 3 a 4 (agrega tabla de traducciones descargadas)
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `downloaded_translations` (
                `translationId` TEXT NOT NULL PRIMARY KEY,
                `downloadedAt` INTEGER NOT NULL
            )
        """)
    }
}

@Database(
    entities = [
        TranslationEntity::class,
        TranslationBookEntity::class,
        TranslationBookChapterEntity::class,
        ReadingProgressEntity::class,
        FavoriteVerseEntity::class,
        DownloadedTranslationEntity::class,
    ],
    exportSchema = false,
    version = 4,
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
    abstract fun favoriteVerseDao(): FavoriteVerseDao
    abstract fun downloadedTranslationDao(): DownloadedTranslationDao
}

internal fun databaseBuilder(context: Context, dbName: String, notifier: RoomNotifier): AppDatabase {
    return Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = dbName,
    )
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
        .fallbackToDestructiveMigration(true)
        .addCallback(notifier)
        .build()
}
