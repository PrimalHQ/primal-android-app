package net.primal

import androidx.sqlite.driver.NativeSQLiteDriver
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.coroutines.IOSDispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.buildPrimalDatabase
import net.primal.data.local.db.getDatabaseBuilder
import org.koin.dsl.module

internal actual fun platformModule() =
    module {
        single<DispatcherProvider> { IOSDispatcherProvider() }

        single<PrimalDatabase> {
            buildPrimalDatabase(
                driver = NativeSQLiteDriver(),
                queryCoroutineContext = get<DispatcherProvider>().io(),
                builder = getDatabaseBuilder(),
            )
        }
    }
