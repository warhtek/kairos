package mobi.kairos.android.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerseTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val verseText = "Foo bar"
    private val referenceText = "Lorep 1:1"

    @Test
    fun `verify verse text is present`() {
        composeRule.setContent {
            Verse(
                title = referenceText,
                text = verseText,
                onClick = {
                    // NOOP
                }
            )
        }
        composeRule.run {
            onNodeWithText(referenceText).assertExists()
            onNodeWithText(verseText).assertExists()
        }
    }

    @Test
    fun `verify verse is clickable`() {
        composeRule.setContent {
            Verse(
                title = referenceText,
                text = verseText,
                onClick = {
                    // NOOP
                }
            )
        }
        composeRule.run {
            onNodeWithText(referenceText).assertExists()
            onNode(hasText(verseText) and hasClickAction())
                .assertExists()
                .performClick()
        }
    }

    @Test
    fun `verify verse with custom modifier`() {
        composeRule.setContent {
            Verse(
                modifier = Modifier,
                title = referenceText,
                text = verseText,
                onClick = {
                    // NOOP
                }
            )
        }

        composeRule.onNodeWithText(verseText).assertExists()
    }
}
