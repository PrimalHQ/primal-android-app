package net.primal

import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.sqlite.driver.AndroidSQLiteDriver
import net.primal.core.utils.coroutines.AndroidDispatcherProvider
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.buildPrimalDatabase
import net.primal.data.local.db.getDatabaseBuilder
import net.primal.networking.config.domain.AppConfig
import net.primal.networking.config.store.AppConfigDataStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual fun platformModule() = module {

    single< DispatcherProvider> { AndroidDispatcherProvider() }

    single<PrimalDatabase> {
        buildPrimalDatabase(
            driver = AndroidSQLiteDriver(),
            queryCoroutineContext = get<DispatcherProvider>().io(),
            builder = getDatabaseBuilder(
                context = get(),
            ),
        )
    }

    single<DataStore<AppConfig>> {
        AppConfigDataStoreFactory.createDataStore {
            androidContext().dataStoreFile(AppConfigDataStoreFactory.APP_CONFIG_DATA_STORE_NAME).path
        }
    }
}
