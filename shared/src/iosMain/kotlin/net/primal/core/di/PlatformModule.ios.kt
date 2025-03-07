package net.primal.core.di

import androidx.sqlite.driver.NativeSQLiteDriver
import net.primal.core.coroutines.DispatcherProvider
import net.primal.core.coroutines.IOSDispatcherProvider
import net.primal.db.PrimalDatabase
import net.primal.db.buildAppDatabase
import net.primal.db.getDatabaseBuilder
import org.koin.dsl.module

internal actual fun platformModule() = module {

    single<DispatcherProvider> { IOSDispatcherProvider() }

    single<PrimalDatabase> {
        buildAppDatabase(
            driver = NativeSQLiteDriver(),
            queryCoroutineContext = get<DispatcherProvider>().io(),
            builder = getDatabaseBuilder(),
        )
    }
}
