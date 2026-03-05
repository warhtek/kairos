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
package mobi.kairos.android.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import mobi.kairos.android.data.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

abstract class BaseDao {
    lateinit var db: AppDatabase

    fun setupInMemoryDb() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    AppDatabase::class.java,
                ).build()
    }
}

@RunWith(RobolectricTestRunner::class)
class DatabaseInfoDaoTest : BaseDao() {
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
