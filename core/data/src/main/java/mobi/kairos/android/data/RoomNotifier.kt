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

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "RoomReadyNotifier"

class RoomReadyNotifier(private val scope: CoroutineScope) : RoomDatabase.Callback() {
    private val _dbReady = MutableSharedFlow<Unit>(replay = 1)
    // val dbReady: SharedFlow<Unit> = _dbReady

    fun notifyReady() = scope.launch {
        _dbReady.emit(Unit)
        Log.d(TAG, "Room database ready notification emitted")
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        scope.launch {
            Log.d(TAG, "Room database version: ${db.version}")
            notifyReady()
        }
    }
}
