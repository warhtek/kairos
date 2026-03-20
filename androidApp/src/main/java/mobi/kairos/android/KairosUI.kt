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
import mobi.kairos.android.ui.books.BooksScreen
import mobi.kairos.android.ui.home.HomeScreen
import mobi.kairos.android.ui.translations.TranslationsScreen

@Composable
fun KairosUI() {
    val navController = rememberNavController()
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var selectedBookName by remember { mutableStateOf<String?>(null) }
    var selectedChapterNumber by remember { mutableStateOf(1) }

    NavHost(
        navController = navController,
        startDestination = KairosNav.Home.route,
    ) {
        composable(KairosNav.Home.route) {
            HomeScreen(
                onNavigateToBooks = {
                    navController.navigate(KairosNav.Books.route)
                },
                onNavigateToTranslations = {
                    navController.navigate(KairosNav.Translations.route)
                },
                selectedBookId = selectedBookId,
                selectedBookName = selectedBookName,
                onBookSelected = {
                    selectedBookId = null
                    selectedBookName = null
                },
            )
        }
        composable(KairosNav.Books.route) {
            BooksScreen(
                onBookClick = { bookId, bookName, chapterNumber ->
                    selectedBookId = bookId
                    selectedBookName = bookName
                    selectedChapterNumber = chapterNumber
                    navController.popBackStack()
                },
            )
        }
        composable(KairosNav.Translations.route) {
            TranslationsScreen(
                onTranslationSelected = { translationId ->
                    // TODO: handle translation change
                    navController.popBackStack()
                },
            )
        }
    }
}
