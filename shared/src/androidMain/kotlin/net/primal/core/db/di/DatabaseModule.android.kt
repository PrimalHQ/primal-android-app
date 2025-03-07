package net.primal.core.db.di

import androidx.sqlite.driver.AndroidSQLiteDriver
import net.primal.core.coroutines.DispatcherProvider
import net.primal.core.db.AppDatabase
import net.primal.core.db.buildAppDatabase
import net.primal.core.db.getDatabaseBuilder
import org.koin.dsl.module

internal actual fun databaseModule() =
    module {
        single<AppDatabase> {
            buildAppDatabase(
                driver = AndroidSQLiteDriver(),
                queryCoroutineContext = get<DispatcherProvider>().io(),
                builder = getDatabaseBuilder(
                    context = get(),
                ),
            )
        }
    }
