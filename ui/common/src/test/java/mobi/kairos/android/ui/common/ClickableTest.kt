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
                }
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
