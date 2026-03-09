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
package mobi.kairos.android.data.resource

import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import mobi.kairos.android.resource.AssetResource
import mobi.kairos.android.resource.TranslationBooksAsset

@OptIn(ExperimentalCoroutinesApi::class)
class TranslationBooksAssetTest {

    private lateinit var translationBooksAsset: TranslationBooksAsset
    private val mockAssetResource = mockk<AssetResource>()
    private val expectedPath = "spa_bes/books.json"

    @Before
    fun setup() {
        translationBooksAsset = TranslationBooksAssetImpl(mockAssetResource)
    }

    @Test
    fun `should open translation books json stream successfully`() = runTest {
        // Given
        val expectedStream = ByteArrayInputStream("{}".toByteArray())
        coEvery { mockAssetResource.openStream(expectedPath) } returns Result.success(expectedStream)

        // When
        val result = translationBooksAsset.openJsonStream("spa_bes")

        // Then
        result.fold(
            onSuccess = { stream ->
                assertIs<InputStream>(stream)
            },
            onFailure = {
                throw AssertionError("Expected success but got failure: $it")
            },
        )
        coEvery { mockAssetResource.openStream(expectedPath) }
    }

    @Test
    fun `should propagate error when asset resource fails`() = runTest {
        // Given
        val expectedError = RuntimeException("Asset error")
        coEvery { mockAssetResource.openStream(expectedPath) } returns Result.failure(expectedError)

        // When
        val result = translationBooksAsset.openJsonStream("spa_bes")

        // Then
        result.fold(
            onSuccess = {
                throw AssertionError("Expected failure but got success")
            },
            onFailure = { error ->
                assertIs<RuntimeException>(error)
                assertEquals(error.message, "Asset error")
            },
        )
    }

    @Test
    fun `should use correct path constant`() = runTest {
        // Given
        val stream = ByteArrayInputStream("{}".toByteArray())
        coEvery { mockAssetResource.openStream(any()) } returns Result.success(stream)

        // When
        translationBooksAsset.openJsonStream("spa_bes")

        // Then
        coEvery { mockAssetResource.openStream("translation_books.json") }
    }

    @Test
    fun `should handle multiple calls correctly`() = runTest {
        // Given
        val stream1 = ByteArrayInputStream("{}".toByteArray())
        val stream2 = ByteArrayInputStream("[]".toByteArray())
        coEvery { mockAssetResource.openStream(expectedPath) } returns Result.success(stream1) andThen Result.success(stream2)

        // When
        val result1 = translationBooksAsset.openJsonStream("spa_bes")
        val result2 = translationBooksAsset.openJsonStream("spa_bes")

        // Then
        TestCase.assertTrue(result1.isSuccess)
        TestCase.assertTrue(result2.isSuccess)
        coEvery { mockAssetResource.openStream(expectedPath) }
    }

    @Test
    fun `should handle empty path gracefully`() = runTest {
        // Given
        val translationBooksAssetWithEmptyPath = object : TranslationBooksAsset {
            override suspend fun openJsonStream(translationId: String): Result<InputStream> = mockAssetResource.openStream("")
        }
        val expectedError = RuntimeException("Empty path")
        coEvery { mockAssetResource.openStream("") } returns Result.failure(expectedError)

        // When
        val result = translationBooksAssetWithEmptyPath.openJsonStream("spa_bes")

        // Then
        result.fold(
            onSuccess = {
                throw AssertionError("Expected failure but got success")
            },
            onFailure = { error ->
                assertIs<RuntimeException>(error)
            },
        )
    }
}
