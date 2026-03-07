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
package mobi.kairos.android.data.repository

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import mobi.kairos.android.data.di.dataModule
import mobi.kairos.android.data.entity.TranslationEntity
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection
import mobi.kairos.android.repository.TranslationRepository

@RunWith(RobolectricTestRunner::class)
class TranslationRepositoryImplTest : KoinTest {
    private lateinit var translationRepository: TranslationRepository

    @Before
    fun setUp() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(dataModule)
        }
        translationRepository = getKoin().get()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `importTranslations should call insertAll on dao with given translations`() = runTest {
        // Given
        val translations = listOf(
            TranslationEntity(
                id = "5L",
                name = "name",
                englishName = "englishName",
                website = "website",
                licenseUrl = "licenseUrl",
                shortName = "shortName",
                language = "language",
                languageName = "languageName",
                languageEnglishName = "languageEnglishName",
                textDirection = TextDirection.LTR,
                availableFormats = listOf(AvailableFormat.JSON),
                listOfBooksApiLink = "listOfBooksApiLink",
                numberOfBooks = 1,
                totalNumberOfChapters = 2,
                totalNumberOfVerses = 3,
                numberOfApocryphalBooks = 4,
                totalNumberOfApocryphalChapters = 5,
                totalNumberOfApocryphalVerses = 6,
            ),
        )

        // When
        translationRepository.importTranslations(translations)
    }
}
