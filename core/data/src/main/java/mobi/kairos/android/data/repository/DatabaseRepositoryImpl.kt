package mobi.kairos.android.data.repository

import mobi.kairos.android.data.dao.DatabaseInfoDao
import mobi.kairos.android.repository.DatabaseRepository

class DatabaseRepositoryImpl(
    private val dao: DatabaseInfoDao
) : DatabaseRepository {
    override suspend fun getVersion(): Int = dao.getVersion()
}
