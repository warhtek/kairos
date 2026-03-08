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
package mobi.kairos.android.data.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import mobi.kairos.android.data.model.TranslationImportModel
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection
import mobi.kairos.android.parser.TranslationJsonParser
import mobi.kairos.android.repository.TranslationRepository
import mobi.kairos.android.resource.TranslationsAsset
import mobi.kairos.android.usecase.ImportTranslationsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ImportTranslationsUseCaseTest {

    private lateinit var useCase: ImportTranslationsUseCase
    private val mockAsset = mockk<TranslationsAsset>()
    private val mockParser = mockk<TranslationJsonParser>()
    private val mockRepository = mockk<TranslationRepository>(relaxed = true)

    @Before
    fun setup() {
        useCase = ImportTranslationsUseCase(
            translationsAsset = mockAsset,
            jsonParser = mockParser,
            translationRepository = mockRepository,
        )
    }

    @Test
    fun `should import translations successfully`() = runTest {
        // Given
        val fakeJson = "[{\"id\":\"spa_r09\"}]"
        val fakeStream = ByteArrayInputStream(fakeJson.toByteArray())
        val fakeTranslations = listOf(
            TranslationImportModel(
                id = "spa_r09",
                name = "Test",
                englishName = "Test",
                website = "",
                licenseUrl = "",
                shortName = "",
                language = "spa",
                languageName = null,
                languageEnglishName = null,
                textDirection = TextDirection.LTR,
                availableFormats = listOf(AvailableFormat.JSON),
                listOfBooksApiLink = "",
                numberOfBooks = 0,
                totalNumberOfChapters = 0,
                totalNumberOfVerses = 0,
                numberOfApocryphalBooks = null,
                totalNumberOfApocryphalChapters = null,
                totalNumberOfApocryphalVerses = null,
            ),
        )

        coEvery { mockAsset.openJsonStream() } returns Result.success(fakeStream)
        coEvery { mockParser.parse(any<InputStream>()) } returns Result.success(fakeTranslations)

        // When
        val result = useCase()

        // Then
        result.fold(
            onSuccess = { summary ->
                assertTrue(summary.success)
                assertEquals(1, summary.count)
                assertTrue(summary.durationMs >= 0)
            },
            onFailure = { fail("Expected success but got failure: ${it.message}") },
        )

        coVerify(exactly = 1) {
            mockAsset.openJsonStream()
            mockParser.parse(any<InputStream>())
            mockRepository.importTranslations(fakeTranslations)
        }
    }

    @Test
    fun `should return failure when asset cannot be opened`() = runTest {
        // Given
        val error = RuntimeException("Asset not found")
        coEvery { mockAsset.openJsonStream() } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        result.fold(
            onSuccess = { fail("Expected failure but got success") },
            onFailure = { exception ->
                assertEquals(error, exception)
            },
        )

        coVerify(exactly = 1) { mockAsset.openJsonStream() }
        coVerify(exactly = 0) { mockParser.parse(any<InputStream>()) }
        coVerify(exactly = 0) { mockRepository.importTranslations(any()) }
    }

    @Test
    fun `should return failure when JSON parsing fails`() = runTest {
        // Given
        val fakeStream = ByteArrayInputStream("[]".toByteArray())
        val error = RuntimeException("Parse error")
        coEvery { mockAsset.openJsonStream() } returns Result.success(fakeStream)
        coEvery { mockParser.parse(any<InputStream>()) } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        result.fold(
            onSuccess = { fail("Expected failure but got success") },
            onFailure = { exception ->
                assertEquals(error, exception)
            },
        )

        coVerify(exactly = 1) {
            mockAsset.openJsonStream()
            mockParser.parse(any<InputStream>())
        }
        coVerify(exactly = 0) { mockRepository.importTranslations(any()) }
    }

    @Test
    fun `should handle unexpected exception in try block`() = runTest {
        // Given
        coEvery { mockAsset.openJsonStream() } throws RuntimeException("Unexpected crash")

        // When
        val result = useCase()

        // Then
        result.fold(
            onSuccess = { fail("Expected failure but got success") },
            onFailure = { exception ->
                assertEquals("Unexpected crash", exception.message)
            },
        )
    }

    @Test
    fun `should import empty list successfully`() = runTest {
        // Given
        val fakeStream = ByteArrayInputStream("[]".toByteArray())
        val emptyList = emptyList<TranslationImportModel>()
        coEvery { mockAsset.openJsonStream() } returns Result.success(fakeStream)
        coEvery { mockParser.parse(any<InputStream>()) } returns Result.success(emptyList)

        // When
        val result = useCase()

        // Then
        result.fold(
            onSuccess = { summary ->
                assertTrue(summary.success)
                assertEquals(0, summary.count)
            },
            onFailure = { fail("Should not fail with empty list") },
        )

        coVerify(exactly = 1) { mockRepository.importTranslations(emptyList) }
    }
}
