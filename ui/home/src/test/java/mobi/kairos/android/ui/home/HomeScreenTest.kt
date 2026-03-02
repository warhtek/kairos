package mobi.kairos.android.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `verify if verse is present in the home screen`() {
        composeRule.setContent {
            HomeScreen()
        }
        composeRule.run {
            onNodeWithText("Lorep 1:1").assertExists()
            onNodeWithText("Lorep ipsum dolor sit amet").assertExists()
        }
    }

    @Test
    fun `verify if top bar text is present in the home screen`() {
        composeRule.setContent {
            HomeScreen()
        }
        composeRule
            .onNodeWithText("KAIROS")
            .assertExists()
    }

    @Test
    fun `verify if verse text is clickable in the home screen`() {
        composeRule.setContent {
            HomeScreen()
        }

        composeRule
            .onNodeWithText("Lorep ipsum dolor sit amet")
            .assertExists()
            .performClick()
    }
}
