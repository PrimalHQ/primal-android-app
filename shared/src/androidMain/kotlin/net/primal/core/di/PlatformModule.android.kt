package net.primal.core.di

import androidx.sqlite.driver.AndroidSQLiteDriver
import net.primal.core.coroutines.AndroidDispatcherProvider
import net.primal.core.coroutines.DispatcherProvider
import net.primal.db.PrimalDatabase
import net.primal.db.buildAppDatabase
import net.primal.db.getDatabaseBuilder
import org.koin.dsl.module

internal actual fun platformModule() = module {

    single<DispatcherProvider> { AndroidDispatcherProvider() }

    single<PrimalDatabase> {
        buildAppDatabase(
            driver = AndroidSQLiteDriver(),
            queryCoroutineContext = get<DispatcherProvider>().io(),
            builder = getDatabaseBuilder(
                context = get(),
            ),
        )
    }
}
