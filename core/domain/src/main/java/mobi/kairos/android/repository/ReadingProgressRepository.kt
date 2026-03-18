package mobi.kairos.android.repository

import mobi.kairos.android.usecase.LastReadVerse

interface ReadingProgressRepository {
    suspend fun getLastRead(): Result<LastReadVerse?>
    suspend fun saveLastRead(verse: LastReadVerse)
}
