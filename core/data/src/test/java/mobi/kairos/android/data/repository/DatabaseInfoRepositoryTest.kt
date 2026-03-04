package mobi.kairos.android.data.repository

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import mobi.kairos.android.data.AppDatabase
import mobi.kairos.android.data.di.dataModule
import mobi.kairos.android.repository.DatabaseRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class DatabaseInfoRepositoryTest {

    private lateinit var databaseRepository: DatabaseRepository

    @Before
    fun setUp() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(dataModule)
        }
        databaseRepository = KoinJavaComponent.getKoin().get()
    }

    @After
    fun tearDown() {
        KoinJavaComponent.getKoin().get<AppDatabase>().close()
        stopKoin()
    }

    @Test
    fun `getVersion returns 1`() = runTest {
        val version = databaseRepository.getVersion()
        assertEquals(1, version)
    }
}