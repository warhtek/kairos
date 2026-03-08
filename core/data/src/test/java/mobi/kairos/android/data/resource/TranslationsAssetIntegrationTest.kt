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
import android.content.Context
import android.content.res.AssetManager
import java.io.ByteArrayInputStream
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import mobi.kairos.android.resource.TranslationsAsset

@OptIn(ExperimentalCoroutinesApi::class)
class TranslationsAssetIntegrationTest {

    private lateinit var translationsAsset: TranslationsAsset
    private lateinit var assetResource: AndroidAssetResource
    private val mockContext = mockk<Context>()
    private val mockAssetManager = mockk<AssetManager>()

    private val expectedPath = "assets/available_translations.json"

    @Before
    fun setup() {
        every { mockContext.assets } returns mockAssetManager
        assetResource = AndroidAssetResource(mockContext)
        translationsAsset = TranslationsAssetImpl(assetResource)
    }

    @Test
    fun `should open real translations json file`() = runTest {
        // Given
        val expectedContent = "[{\"id\":\"test\"}]"
        val expectedStream = ByteArrayInputStream(expectedContent.toByteArray())
        every { mockAssetManager.open(expectedPath) } returns expectedStream

        // When
        val result = translationsAsset.openJsonStream()

        // Then
        result.fold(
            onSuccess = { stream ->
                val content = stream.bufferedReader().use { it.readText() }
                assertTrue(content.isNotEmpty())
                assertTrue(content.contains("test"))
            },
            onFailure = {
                throw AssertionError("Expected success but got failure: $it")
            },
        )
    }

    @Test
    fun `should handle large JSON file`() = runTest {
        // Given
        val largeContent = "[{\"id\":\"test\"}]".repeat(1000)
        val expectedStream = ByteArrayInputStream(largeContent.toByteArray())
        every { mockAssetManager.open(expectedPath) } returns expectedStream

        // When
        val result = translationsAsset.openJsonStream()

        // Then
        result.fold(
            onSuccess = { stream ->
                assertTrue(stream.available() > 0)
                // Do not read the whole content, just verify that the strep is actually open
            },
            onFailure = {
                throw AssertionError("Expected success but got failure: $it")
            },
        )
    }
}
