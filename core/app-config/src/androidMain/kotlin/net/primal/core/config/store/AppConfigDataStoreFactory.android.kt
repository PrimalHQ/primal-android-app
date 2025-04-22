package net.primal.core.config.store

import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import net.primal.domain.global.AppConfig

internal actual fun createAppConfigDataStorePersistence(dataStoreFileName: String): DataStore<AppConfig> {
    val appContext = AppConfigInitializer.appContext
        ?: error("AppConfig not initialized. Please call AppConfigInitializer.init(ApplicationContext).")

    return createDataStore {
        appContext.dataStoreFile(dataStoreFileName).path
    }
}
