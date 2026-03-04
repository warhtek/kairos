package mobi.kairos.android.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import mobi.kairos.android.data.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

abstract class BaseDao {
    lateinit var db: AppDatabase
    fun setupInMemoryDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }
}

@RunWith(RobolectricTestRunner::class)
class DatabaseInfoDaoTest: BaseDao() {
    private lateinit var databaseInfoDao: DatabaseInfoDao

    @Before
    fun setUp() {
        setupInMemoryDb()
        databaseInfoDao = db.databaseInfoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getVersion returns 1`() = runTest {
        val version = databaseInfoDao.getVersion()
        assertEquals(1, version)
    }
}