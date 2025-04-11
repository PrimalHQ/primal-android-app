package net.primal.core.config.store

import androidx.datastore.core.DataStore
import net.primal.domain.global.AppConfig

internal actual fun createAppConfigDataStorePersistence(dataStoreFileName: String): DataStore<AppConfig> {
    return createDataStore {
        throw NotImplementedError()
    }
}
