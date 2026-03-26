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

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import mobi.kairos.android.ui.home.HomeScreen
import mobi.kairos.android.ui.splash.SplashScreen

@Composable
fun KairosUI() {
    val navController = rememberNavController()

    // State for navigation from Splash
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var selectedBookName by remember { mutableStateOf<String?>(null) }
    var selectedChapterNumber by remember { mutableStateOf(1) }
    var selectedVerseNumber by remember { mutableStateOf(1) }
    var currentTranslationId by remember { mutableStateOf<String?>(null) }

    // Flag to navigate from Home back to Splash
    var navigateToSplashFromHome by remember { mutableStateOf(false) }

    // Flag to track if HomeScreen has been initialized
    var homeScreenInitialized by remember { mutableStateOf(false) }

    // Effect to navigate from Home to Splash
    LaunchedEffect(navigateToSplashFromHome) {
        if (navigateToSplashFromHome) {
            navigateToSplashFromHome = false
            // Clear selections when returning to splash
            selectedBookId = null
            selectedBookName = null
            selectedChapterNumber = 1
            selectedVerseNumber = 1
            homeScreenInitialized = false
            navController.navigate(KairosNav.Splash.route) {
                popUpTo(KairosNav.Home.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = KairosNav.Splash.route,
    ) {
        composable(KairosNav.Splash.route) {
            SplashScreen(
                onDailyVerseClick = { bookId, bookName, chapterNumber, verseNumber ->
                    Log.d("KairosUI", "Daily verse clicked: $bookName $chapterNumber:$verseNumber")
                    // Update state with selected verse
                    selectedBookId = bookId
                    selectedBookName = bookName
                    selectedChapterNumber = chapterNumber
                    selectedVerseNumber = verseNumber
                   // homeScreenInitialized = false // Reset flag for new navigation
                    // Navigate to home
                    navController.navigate(KairosNav.Home.route) {
                        popUpTo(KairosNav.Splash.route) { inclusive = true }
                    }
                },
                onBibleClick = {
                    Log.d("KairosUI", "Bible button clicked - navigating to Bible home screen")
                    // Clear selections to go to home without specific verse
                    selectedBookId = null
                    selectedBookName = null
                    selectedChapterNumber = 1
                    selectedVerseNumber = 1
                    //homeScreenInitialized = false // Reset flag for new navigation
                    // Navigate to home
                    navController.navigate(KairosNav.Home.route) {
                        popUpTo(KairosNav.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(KairosNav.Home.route) {
            // Add a loading state or delay to prevent UI freezes
            LaunchedEffect(Unit) {
                if (!homeScreenInitialized) {
                    // Small delay to allow UI to settle
                    delay(50)
                    homeScreenInitialized = true
                }
            }

            HomeScreen(
                selectedBookId = selectedBookId,
                selectedBookName = selectedBookName,
                selectedChapterNumber = selectedChapterNumber,
                selectedVerseNumber = selectedVerseNumber,
                selectedTranslationId = currentTranslationId,
                onBookSelected = {
                    Log.d("KairosUI", "Book selected, clearing verse selection")
                    // Clear selection after navigating from a book
                    selectedBookId = null
                    selectedBookName = null
                    selectedChapterNumber = 1
                    selectedVerseNumber = 1
                },
                onTranslationChanged = {
                    Log.d("KairosUI", "Translation changed")
                    currentTranslationId = null
                },
                onNavigateToSplash = {
                    Log.d("KairosUI", "Navigating back to splash")
                    navigateToSplashFromHome = true
                },
            )
        }
    }
}
