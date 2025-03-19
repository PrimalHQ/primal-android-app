package net.primal.core.config.store

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import net.primal.domain.AppConfig
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

internal fun createDataStore(producePath: () -> String): DataStore<AppConfig> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = AppConfigSerialization,
            producePath = { producePath().toPath() },
        ),
    )

internal expect fun createAppConfigDataStorePersistence(dataStoreFileName: String): DataStore<AppConfig>
