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

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import mobi.kairos.android.di.appModule
import mobi.kairos.android.data.di.dataModule
import mobi.kairos.android.di.domainModule
import mobi.kairos.android.ui.books.booksModule
import mobi.kairos.android.ui.home.homeModule
import mobi.kairos.android.ui.search.searchModule
import mobi.kairos.android.ui.splash.splashModule
import mobi.kairos.android.ui.translations.translationsModule


class MainApplication : Application() {

    companion object {
        private var instance: MainApplication? = null

        fun getInstance(): MainApplication? = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                appModule,
                dataModule,
                domainModule,
                homeModule,
                booksModule,
                splashModule,
                searchModule,
                translationsModule
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainApplication", "Configuration changed: ${newConfig.locale?.displayName}")
        // Notificar a los componentes que el idioma ha cambiado
        // El refresco del TTS se maneja en MainActivity
    }
}
