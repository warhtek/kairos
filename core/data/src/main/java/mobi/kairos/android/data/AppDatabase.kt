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
import kotlinx.coroutines.CoroutineScope
import mobi.kairos.android.data.dao.DatabaseInfoDao
import mobi.kairos.android.data.entity.ContentEntity

@Database(
    entities = [ContentEntity::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseInfoDao(): DatabaseInfoDao
}

internal fun databaseBuilder(context: Context, dbName: String, notifier: RoomReadyNotifier): AppDatabase = Room
    .databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = dbName,
    ).fallbackToDestructiveMigration(true)
    .addCallback(notifier)
    .build()

internal fun databaseReadyNotifierBuilder(scope: CoroutineScope): RoomReadyNotifier = RoomReadyNotifier(scope)
