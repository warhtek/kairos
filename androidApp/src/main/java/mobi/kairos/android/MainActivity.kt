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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mobi.kairos.android.usecase.GetDatabaseVersionUseCase
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val getDatabaseVersion: GetDatabaseVersionUseCase by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val version = getDatabaseVersion()
            Log.d("MainActivity", "DB version: $version")
        }

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val colorScheme =
                when {
                    supportsDynamicColor -> {
                        if (darkTheme) {
                            dynamicDarkColorScheme(this)
                        } else {
                            dynamicLightColorScheme(this)
                        }
                    }

                    else -> {
                        if (darkTheme) {
                            darkColorScheme()
                        } else {
                            lightColorScheme()
                        }
                    }
                }

            MaterialTheme(colorScheme = colorScheme) {
                KairosUI()
            }
        }
    }
}
