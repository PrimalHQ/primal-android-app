package net.primal.core.config.store

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.AppConfig

internal class AppConfigDataStore(
    dispatcherProvider: DispatcherProvider,
    private val persistence: DataStore<AppConfig>,
) {

    private val scope = CoroutineScope(dispatcherProvider.io())

    val config = persistence.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first() },
        )

    suspend fun updateConfig(reducer: AppConfig.() -> AppConfig): AppConfig {
        return persistence.updateData { currentAppConfig ->
            val newAppConfig = currentAppConfig.reducer()
            if (!currentAppConfig.cacheUrlOverride) {
                newAppConfig
            } else {
                currentAppConfig.copy(
                    uploadUrl = newAppConfig.uploadUrl,
                    walletUrl = newAppConfig.walletUrl,
                )
            }
        }
    }

    suspend fun overrideCacheUrl(url: String) {
        persistence.updateData {
            it.copy(cacheUrl = url, cacheUrlOverride = true)
        }
    }

    suspend fun revertCacheUrlOverrideFlag() {
        persistence.updateData {
            it.copy(cacheUrlOverride = false)
        }
    }
}
