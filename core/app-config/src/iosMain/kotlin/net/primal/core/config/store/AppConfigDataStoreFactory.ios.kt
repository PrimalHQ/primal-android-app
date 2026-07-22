package net.primal.core.config.store

import androidx.datastore.core.DataStore
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.core.utils.files.excludeFromBackup
import net.primal.domain.global.AppConfig
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

private const val DATASTORE_DIRECTORY_NAME = "datastore"

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
        val documentsPath = requireNotNull(documentDirectory?.path)
        val directoryPath = "$documentsPath/$DATASTORE_DIRECTORY_NAME"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(directoryPath)) {
            fileManager.createDirectoryAtPath(
                path = directoryPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        excludeFromBackup(directoryPath)

        val targetPath = "$directoryPath/$dataStoreFileName"
        val legacyPath = "$documentsPath/$dataStoreFileName"
        if (fileManager.fileExistsAtPath(legacyPath) && !fileManager.fileExistsAtPath(targetPath)) {
            fileManager.moveItemAtPath(legacyPath, toPath = targetPath, error = null)
        }
        targetPath
    }
}
