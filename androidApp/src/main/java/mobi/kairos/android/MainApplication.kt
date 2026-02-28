/**
 * Copyright (C) 2026 MOBIWARE
 *
 * This software and its source code are the exclusive property of MOBIWARE.
 *
 * Any unauthorized use, copying, distribution, or modification of this software, in whole or in part,
 * may result in severe civil and criminal penalties under applicable copyright and trade secret laws.
 */
package mobi.kairos.android

import android.app.Application
import mobi.kairos.android.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(dataModule)
        }
    }
}