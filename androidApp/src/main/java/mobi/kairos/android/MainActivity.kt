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

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mobi.kairos.android.data.RoomReadyNotifier
import mobi.kairos.android.usecase.GetDatabaseVersionUseCase
import mobi.kairos.android.usecase.ImportChaptersUseCase
import mobi.kairos.android.usecase.ImportTranslationBooksUseCase
import mobi.kairos.android.usecase.ImportTranslationsUseCase
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import mobi.kairos.android.repository.ChapterRepository

class MainActivity : ComponentActivity() {
    private val roomReadyNotifier: RoomReadyNotifier by inject()
    private val getDatabaseVersion: GetDatabaseVersionUseCase by inject()
    private val importTranslations: ImportTranslationsUseCase by inject()
    private val importTranslationBooks: ImportTranslationBooksUseCase by inject()
    private val importChapters: ImportChaptersUseCase by inject()
    private val appScope: CoroutineScope by inject(named("appScope"))

    private val chapterRepository: ChapterRepository by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            launchAppDatabaseSetup()
        }

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val colorScheme = when {
                supportsDynamicColor -> if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
                else -> if (darkTheme) darkColorScheme() else lightColorScheme()
            }
            MaterialTheme(colorScheme = colorScheme) {
                KairosUI()
            }
        }
    }

    private suspend fun launchAppDatabaseSetup() = runCatching {
        val version = getDatabaseVersion()
        Log.d("MainActivity", "DB version: $version")

        roomReadyNotifier.dbReady.first()
        Log.d("MainActivity", "DB ready for operations")

        if (chapterRepository.count() == 0) {
            appScope.launch {
                Toast.makeText(this@MainActivity, "Un momento, preparando la base de datos", Toast.LENGTH_LONG).show()
            }
            importTranslations().onSuccess {
                Log.d("MainActivity", "Imported ${it.count} translations in ${it.durationMs} ms")
            }
            importTranslationBooks(listOf("spa_bes")).onSuccess {
                Log.d("MainActivity", "Imported ${it.count} books in ${it.durationMs} ms")
            }
            importChapters("spa_bes")
                .onSuccess {
                    Log.d("MainActivity", "Imported ${it.count} chapters in ${it.durationMs} ms")
                }
                .onFailure {
                    Log.e("MainActivity", "Failed to import chapters", it)
                }
        }
    }
}
