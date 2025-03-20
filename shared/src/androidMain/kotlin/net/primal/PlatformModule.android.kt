package net.primal

import androidx.sqlite.driver.AndroidSQLiteDriver
import net.primal.core.utils.coroutines.AndroidDispatcherProvider
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.buildPrimalDatabase
import net.primal.data.local.db.getDatabaseBuilder
import org.koin.dsl.module

internal actual fun platformModule() =
    module {
        single<DispatcherProvider> { AndroidDispatcherProvider() }

        single<PrimalDatabase> {
            buildPrimalDatabase(
                driver = AndroidSQLiteDriver(),
                queryCoroutineContext = get<DispatcherProvider>().io(),
                builder = getDatabaseBuilder(
                    context = get(),
                ),
            )
        }
    }
