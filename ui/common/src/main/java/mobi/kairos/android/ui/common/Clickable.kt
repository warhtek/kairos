package mobi.kairos.android.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Clickable(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun ClickablePreview() {
    MaterialTheme {
        Clickable(
            onClick = {
                // NOOP
            }
        ) {
            Text("Click me!")
        }
    }
}

