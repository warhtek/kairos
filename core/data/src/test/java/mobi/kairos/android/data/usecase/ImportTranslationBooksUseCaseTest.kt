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
import kotlinx.serialization.InternalSerializationApi
import org.junit.Before
import org.junit.Test
import mobi.kairos.android.data.model.TranslationBookModel
import mobi.kairos.android.parser.TranslationBookJsonParser
import mobi.kairos.android.repository.TranslationBookRepository
import mobi.kairos.android.resource.TranslationBooksAsset
import mobi.kairos.android.usecase.ImportTranslationBooksUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ImportTranslationBooksUseCaseTest {

    private lateinit var useCase: ImportTranslationBooksUseCase
    private val mockAsset = mockk<TranslationBooksAsset>()
    private val mockParser = mockk<TranslationBookJsonParser>()
    private val mockRepository = mockk<TranslationBookRepository>(relaxed = true)

    @Before
    fun setup() {
        useCase = ImportTranslationBooksUseCase(
            translationBooksAsset = mockAsset,
            jsonParser = mockParser,
            translationBookRepository = mockRepository,
        )
    }

    @OptIn(InternalSerializationApi::class)
    @Test
    fun `should import books successfully`() = runTest {
        // Given
        val fakeJson = "[{\"id\":\"gen\"}]"
        val fakeStream = ByteArrayInputStream(fakeJson.toByteArray())
        val fakeBooks = listOf(
            TranslationBookModel(
                id = "gen",
                name = "Genesis",
                commonName = "Genesis",
                title = "The Book of Genesis",
                order = 1,
                numberOfChapters = 50,
                firstChapterNumber = 1,
                firstChapterApiLink = "",
                lastChapterNumber = 50,
                lastChapterApiLink = "",
                totalNumberOfVerses = 1533,
                isApocryphal = false,
            ),
        )

        coEvery { mockAsset.openJsonStream() } returns Result.success(fakeStream)
        coEvery { mockParser.parse(any<InputStream>()) } returns Result.success(fakeBooks)

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
            mockRepository.importBooks(fakeBooks)
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
        coVerify(exactly = 0) { mockRepository.importBooks(any()) }
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
        coVerify(exactly = 0) { mockRepository.importBooks(any()) }
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

    @OptIn(InternalSerializationApi::class)
    @Test
    fun `should import empty list successfully`() = runTest {
        // Given
        val fakeStream = ByteArrayInputStream("[]".toByteArray())
        val emptyList = emptyList<TranslationBookModel>()
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

        coVerify(exactly = 1) { mockRepository.importBooks(emptyList) }
    }
}
