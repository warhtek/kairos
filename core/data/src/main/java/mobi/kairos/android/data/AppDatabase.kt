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
package mobi.kairos.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mobi.kairos.android.data.dao.DatabaseInfoDao
import mobi.kairos.android.data.dao.TranslationBookDao
import mobi.kairos.android.data.dao.TranslationDao
import mobi.kairos.android.data.entity.TranslationBookEntity
import mobi.kairos.android.data.entity.TranslationEntity

@Database(
    entities = [TranslationEntity::class, TranslationBookEntity::class],
    exportSchema = false,
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseInfoDao(): DatabaseInfoDao
    abstract fun translationDao(): TranslationDao
    abstract fun translationBookDao(): TranslationBookDao
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
