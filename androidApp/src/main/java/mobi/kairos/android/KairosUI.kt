package mobi.kairos.android

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mobi.kairos.android.ui.home.HomeScreen

@Composable
fun KairosUI() {
    NavHost(
        navController = rememberNavController(),
        startDestination = KairosNav.Home.route
    ) {
        composable(KairosNav.Home.route) {
            HomeScreen()
        }
    }
}

