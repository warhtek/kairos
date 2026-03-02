package mobi.kairos.android.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import mobi.kairos.android.ui.common.Verse

@Composable
fun HomeScreen() {
    Scaffold(
        topBar = { KairosTopBar() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Verse(
                modifier = Modifier
                     .padding(24.dp)
                    .align(Alignment.Center),
                title = "Lorep 1:1",
                text = "Lorep ipsum dolor sit amet",
                onClick = {
                    // NOOP
                }
            )
        }
    }
}
