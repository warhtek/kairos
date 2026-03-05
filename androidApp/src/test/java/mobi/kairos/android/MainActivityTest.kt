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
package mobi.kairos.android

import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `test low API level with light theme`() {
        assertTextNodeExists()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M], qualifiers = "night")
    fun `test low API level with dark theme`() {
        assertTextNodeExists()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `test high API level with light theme`() {
        assertTextNodeExists()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S], qualifiers = "night")
    fun `test high API level with dark theme`() {
        assertTextNodeExists()
    }

    private fun assertTextNodeExists(text: String = "KAIROS") {
        composeTestRule.onNodeWithText(text).assertExists()
    }
}
