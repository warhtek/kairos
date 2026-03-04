package mobi.kairos.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mobi.kairos.android.data.dao.DatabaseInfoDao
import mobi.kairos.android.data.di.DbInitNotifier
import mobi.kairos.android.data.entity.ContentEntity

@Database(
    entities = [ContentEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseInfoDao(): DatabaseInfoDao
}

internal fun databaseBuilder(
    context: Context,
    dbName: String,
    nofifier: DbInitNotifier,
): AppDatabase {
    return Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = dbName
    )
        .fallbackToDestructiveMigration(true)
        .build()
        .also {
            nofifier.notifyReady()
        }
}
