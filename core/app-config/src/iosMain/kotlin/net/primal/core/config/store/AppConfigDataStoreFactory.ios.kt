package net.primal.core.config.store

import androidx.datastore.core.DataStore
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.domain.global.AppConfig
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun createAppConfigDataStorePersistence(dataStoreFileName: String): DataStore<AppConfig> {
    return createDataStore {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        requireNotNull(documentDirectory).path + "/$dataStoreFileName"
    }
}
