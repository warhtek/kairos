package mobi.kairos.android.repository

interface DatabaseRepository {
    suspend fun getVersion(): Int
}
