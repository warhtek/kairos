/**
 * Copyright (C) 2026 MOBIWARE
 *
 * This software and its source code are the exclusive property of MOBIWARE.
 *
 * Any unauthorized use, copying, distribution, or modification of this software, in whole or in part,
 * may result in severe civil and criminal penalties under applicable copyright and trade secret laws.
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
            val colorScheme = when {
                supportsDynamicColor -> {
                    if (darkTheme) dynamicDarkColorScheme(this)
                    else dynamicLightColorScheme(this)
                }
                else -> {
                    if (darkTheme) darkColorScheme()
                    else lightColorScheme()
                }
            }

            MaterialTheme(colorScheme = colorScheme) {
                KairosUI()
            }
        }
    }
}
