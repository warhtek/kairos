package mobi.kairos.android.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module
import mobi.kairos.android.data.di.dataModule

val appModule = module {
        single<CoroutineScope>(named("appScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
        includes(domainModule, dataModule)
    }
