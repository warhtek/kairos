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
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import mobi.kairos.android.resource.AssetResource
import mobi.kairos.android.resource.TranslationsAsset

@OptIn(ExperimentalCoroutinesApi::class)
class TranslationsAssetTest {

    private lateinit var translationsAsset: TranslationsAsset
    private val mockAssetResource = mockk<AssetResource>()
    private val expectedPath = "available_translations.json"

    @Before
    fun setup() {
        translationsAsset = TranslationsAssetImpl(mockAssetResource)
    }

    @Test
    fun `should open translations json stream successfully`() = runTest {
        // Given
        val expectedStream = ByteArrayInputStream("{}".toByteArray())
        coEvery { mockAssetResource.openStream(expectedPath) } returns Result.success(expectedStream)

        // When
        val result = translationsAsset.openJsonStream()

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
        val result = translationsAsset.openJsonStream()

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
        translationsAsset.openJsonStream()

        // Then
        coEvery { mockAssetResource.openStream("available_translations.json") }
    }

    @Test
    fun `should handle multiple calls correctly`() = runTest {
        // Given
        val stream1 = ByteArrayInputStream("{}".toByteArray())
        val stream2 = ByteArrayInputStream("[]".toByteArray())
        coEvery { mockAssetResource.openStream(expectedPath) } returns Result.success(stream1) andThen Result.success(stream2)

        // When
        val result1 = translationsAsset.openJsonStream()
        val result2 = translationsAsset.openJsonStream()

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        coEvery { mockAssetResource.openStream(expectedPath) }
    }

    @Test
    fun `should handle empty path gracefully`() = runTest {
        // Given
        val translationsAssetWithEmptyPath = object : TranslationsAsset {
            override suspend fun openJsonStream(): Result<InputStream> = mockAssetResource.openStream("")
        }
        val expectedError = RuntimeException("Empty path")
        coEvery { mockAssetResource.openStream("") } returns Result.failure(expectedError)

        // When
        val result = translationsAssetWithEmptyPath.openJsonStream()

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
