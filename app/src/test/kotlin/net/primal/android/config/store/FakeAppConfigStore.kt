package net.primal.android.config.store

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import net.primal.android.config.domain.AppConfig
import net.primal.android.config.domain.DEFAULT_APP_CONFIG

class FakeAppConfigStore(initialAppConfig: AppConfig = DEFAULT_APP_CONFIG): DataStore<AppConfig> {

    var latestAppConfig: AppConfig = initialAppConfig

    override val data: Flow<AppConfig> = flowOf(initialAppConfig)

    override suspend fun updateData(transform: suspend (t: AppConfig) -> AppConfig): AppConfig {
        latestAppConfig = transform(latestAppConfig)
        return latestAppConfig
    }
}
