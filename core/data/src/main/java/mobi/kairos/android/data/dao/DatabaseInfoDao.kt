package mobi.kairos.android.data.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface DatabaseInfoDao {
    @RawQuery
    suspend fun rawQuery(query: SupportSQLiteQuery): Int

    suspend fun getVersion(): Int {
        return rawQuery(SimpleSQLiteQuery("PRAGMA user_version"))
    }
}