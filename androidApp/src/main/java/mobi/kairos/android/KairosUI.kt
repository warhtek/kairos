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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mobi.kairos.android.ui.home.HomeScreen
import mobi.kairos.android.ui.splash.SplashScreen

@Composable
fun KairosUI() {
    val navController = rememberNavController()
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var selectedBookName by remember { mutableStateOf<String?>(null) }
    var selectedChapterNumber by remember { mutableStateOf(1) }
    var selectedVerseNumber by remember { mutableStateOf(1) }
    var currentTranslationId by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = KairosNav.Splash.route,
    ) {
        composable(KairosNav.Splash.route) {
            SplashScreen(
                onDailyVerseClick = { bookId, bookName, chapterNumber, verseNumber ->
                    selectedBookId = bookId
                    selectedBookName = bookName
                    selectedChapterNumber = chapterNumber
                    selectedVerseNumber = verseNumber
                    navController.navigate(KairosNav.Home.route) {
                        popUpTo(KairosNav.Splash.route) { inclusive = true }
                    }
                },
                onBibleClick = {
                    // Navegar al home sin seleccionar ningún versículo específico
                    selectedBookId = null
                    selectedBookName = null
                    selectedChapterNumber = 1
                    selectedVerseNumber = 1
                    navController.navigate(KairosNav.Home.route) {
                        popUpTo(KairosNav.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(KairosNav.Home.route) {
            HomeScreen(
                selectedBookId = selectedBookId,
                selectedBookName = selectedBookName,
                selectedChapterNumber = selectedChapterNumber,
                selectedVerseNumber = selectedVerseNumber,
                selectedTranslationId = currentTranslationId,
                onBookSelected = {
                    selectedBookId = null
                    selectedBookName = null
                    selectedChapterNumber = 1
                    selectedVerseNumber = 1
                },
                onTranslationChanged = {
                    currentTranslationId = null
                },
            )
        }
    }
}
