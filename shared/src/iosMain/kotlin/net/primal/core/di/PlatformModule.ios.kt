package net.primal.core.di

import androidx.datastore.core.DataStore
import androidx.sqlite.driver.NativeSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.core.coroutines.DispatcherProvider
import net.primal.core.coroutines.IOSDispatcherProvider
import net.primal.db.PrimalDatabase
import net.primal.db.buildPrimalDatabase
import net.primal.db.getDatabaseBuilder
import net.primal.networking.config.domain.AppConfig
import net.primal.networking.config.store.AppConfigDataStoreFactory
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformModule() = module {

    single<DispatcherProvider> { IOSDispatcherProvider() }

    single<PrimalDatabase> {
        buildPrimalDatabase(
            driver = NativeSQLiteDriver(),
            queryCoroutineContext = get<DispatcherProvider>().io(),
            builder = getDatabaseBuilder(),
        )
    }

    single<DataStore<AppConfig>> {
        AppConfigDataStoreFactory.createDataStore {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/${AppConfigDataStoreFactory.APP_CONFIG_DATA_STORE_NAME}"
        }
    }
}
