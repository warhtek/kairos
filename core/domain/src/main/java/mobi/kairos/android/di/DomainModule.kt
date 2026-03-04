package mobi.kairos.android.di

import mobi.kairos.android.usecase.GetDatabaseVersionUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetDatabaseVersionUseCase(get()) }
}
