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
package mobi.kairos.android.ui.common

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClickableTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `verify click`() {
        composeRule.setContent {
            Clickable(
                onClick = {
                    // NOOP
                },
            ) {
                Text("Click me!")
            }
        }
        composeRule.run {
            onNodeWithText("Click me!")
                .assertExists()
                .performClick()
        }
    }
}
