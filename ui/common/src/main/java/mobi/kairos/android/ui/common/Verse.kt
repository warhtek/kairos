package mobi.kairos.android.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Verse(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Clickable(
            onClick = onClick
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VersePreview() {
    MaterialTheme {
        Verse(
            title = "Lorep 1:1",
            text = "Lorep Ipsum",
            onClick = {
                // NOOP
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerseFullSizePreview() {
    MaterialTheme {
        Verse(
            title = "Lorep 1:1",
            text = "Lorep Ipsum",
            onClick = {
                // NOOP
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        )
    }
}
