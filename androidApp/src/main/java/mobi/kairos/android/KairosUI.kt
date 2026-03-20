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
import mobi.kairos.android.ui.search.SearchScreen
import mobi.kairos.android.ui.translations.TranslationsScreen

@Composable
fun KairosUI() {
    val navController = rememberNavController()
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var selectedBookName by remember { mutableStateOf<String?>(null) }
    var selectedChapterNumber by remember { mutableStateOf(1) }
    var selectedVerseNumber by remember { mutableStateOf(1) }
    var currentTranslationId by remember { mutableStateOf("spa_bes") }

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
                onNavigateToSearch = {
                    navController.navigate(KairosNav.Search.route)
                },
                selectedBookId = selectedBookId,
                selectedBookName = selectedBookName,
                selectedChapterNumber = selectedChapterNumber,
                selectedVerseNumber = selectedVerseNumber,
                onBookSelected = {
                    selectedBookId = null
                    selectedBookName = null
                    selectedChapterNumber = 1
                    selectedVerseNumber = 1
                },
            )
        }
        composable(KairosNav.Books.route) {
            BooksScreen(
                onBookClick = { bookId, bookName, chapterNumber ->
                    selectedBookId = bookId
                    selectedBookName = bookName
                    selectedChapterNumber = chapterNumber
                    selectedVerseNumber = 1
                    navController.popBackStack()
                },
            )
        }
        composable(KairosNav.Translations.route) {
            TranslationsScreen(
                onTranslationSelected = { translationId ->
                    currentTranslationId = translationId
                    navController.popBackStack()
                },
            )
        }
        composable(KairosNav.Search.route) {
            SearchScreen(
                translationId = currentTranslationId,
                onResultClick = { bookId, bookName, chapterNumber, verseNumber ->
                    selectedBookId = bookId
                    selectedBookName = bookName
                    selectedChapterNumber = chapterNumber
                    selectedVerseNumber = verseNumber
                    navController.popBackStack()
                },
            )
        }
    }
}
