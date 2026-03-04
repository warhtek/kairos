package mobi.kairos.android.data.di

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "DbInitNotifier"

class DbInitNotifier(
    private val scope: CoroutineScope
) {
    private val _dbReady = MutableSharedFlow<Unit>(replay = 1)
    // val dbReady: SharedFlow<Unit> = _dbReady

    fun notifyReady() = scope.launch {
        _dbReady.emit(Unit)
        Log.d(TAG, "Room database ready notification emitted")
    }
}
