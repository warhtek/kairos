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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import android.content.Context
import android.content.res.AssetManager
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidAssetResourceTest {

    private lateinit var assetResource: AndroidAssetResource
    private val mockContext = mockk<Context>()
    private val mockAssetManager = mockk<AssetManager>()

    @Before
    fun setup() {
        every { mockContext.assets } returns mockAssetManager
        assetResource = AndroidAssetResource(mockContext)
    }

    @Test
    fun `should open stream successfully`() = runTest {
        // Given
        val path = "test/file.json"
        val expectedStream = ByteArrayInputStream("{}".toByteArray())
        every { mockAssetManager.open(path) } returns expectedStream

        // When
        val result = assetResource.openStream(path)

        // Then
        result.fold(
            onSuccess = { stream ->
                assertIs<InputStream>(stream)
                assertTrue(stream.available() > 0)
            },
            onFailure = { error ->
                throw AssertionError("Expected success but got failure: $error")
            },
        )
        verify(exactly = 1) { mockAssetManager.open(path) }
    }

    @Test
    fun `should return failure when file not found`() = runTest {
        // Given
        val path = "nonexistent/file.json"
        every { mockAssetManager.open(path) } throws RuntimeException("File not found")

        // When
        val result = assetResource.openStream(path)

        // Then
        result.fold(
            onSuccess = {
                throw AssertionError("Expected failure but got success")
            },
            onFailure = { error ->
                assertIs<RuntimeException>(error)
                assertTrue(error.message?.contains("not found") == true)
            },
        )
    }

    @Test
    fun `should handle IO dispatcher correctly`() = runTest {
        // Given
        val path = "test/file.json"
        val expectedStream = ByteArrayInputStream("{}".toByteArray())
        every { mockAssetManager.open(path) } returns expectedStream

        // When
        val result = assetResource.openStream(path)

        // Then
        assertTrue(result.isSuccess)
        // Verificar que la operación se completa (el dispatcher IO se usa internamente)
    }
}
