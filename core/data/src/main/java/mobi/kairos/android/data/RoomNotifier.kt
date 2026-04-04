package mobi.kairos.android.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoomNotifier : RoomDatabase.Callback() {
    private val _dbReady = MutableStateFlow(false)
    val dbReady: StateFlow<Boolean> = _dbReady.asStateFlow()
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        _dbReady.value = true
    }
    
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        _dbReady.value = true
    }
}
