package net.primal.networking.config.store

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import net.primal.networking.config.domain.AppConfig
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

internal object AppConfigDataStoreFactory {

    internal const val APP_CONFIG_DATA_STORE_NAME = "app_config.json"

    fun createDataStore(producePath: () -> String): DataStore<AppConfig> =
        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = AppConfigSerialization,
                producePath = { producePath().toPath() },
            ),
        )

}
