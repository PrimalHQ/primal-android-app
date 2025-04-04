package net.primal.core.config

import io.github.aakira.napier.Napier
import kotlin.time.Duration
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.primal.core.config.api.ApiConfigResponse
import net.primal.core.config.api.WellKnownApi
import net.primal.core.config.store.AppConfigDataStore
import net.primal.core.utils.coroutines.DispatcherProvider

internal class AppConfigHandlerImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val appConfigStore: AppConfigDataStore,
    private val wellKnownApi: WellKnownApi,
) : AppConfigHandler {

    private val fetchMutex = Mutex()
    private var lastTimeFetched: Instant = Instant.DISTANT_PAST

    private fun isAppConfigSyncedInLast(duration: Duration): Boolean {
        return lastTimeFetched < Clock.System.now().minus(duration)
    }

    override suspend fun updateAppConfigOrFailSilently() =
        fetchMutex.withLock {
            val response = fetchAppConfigOrNull() ?: return@withLock
            lastTimeFetched = Clock.System.now()
            appConfigStore.updateConfig {
                copy(
                    cacheUrl = response.cacheServers.firstOrNull() ?: this.cacheUrl,
                    uploadUrl = response.uploadServers.firstOrNull() ?: this.uploadUrl,
                    walletUrl = response.walletServers.firstOrNull() ?: this.walletUrl,
                )
            }
        }

    override suspend fun updateAppConfigWithDebounce(duration: Duration) =
        fetchMutex.withLock {
            if (isAppConfigSyncedInLast(duration)) {
                updateAppConfigOrFailSilently()
            }
        }

    private suspend fun fetchAppConfigOrNull(): ApiConfigResponse? {
        val result = runCatching {
            withContext(dispatcherProvider.io()) { wellKnownApi.fetchApiConfig() }
        }
        result.exceptionOrNull()?.let { Napier.w("Unable to fetch app config", it) }
        return result.getOrNull()
    }

    override suspend fun overrideCacheUrl(url: String) = appConfigStore.overrideCacheUrl(url = url)

    override suspend fun restoreDefaultCacheUrl() {
        val response = fetchAppConfigOrNull()
        val wellKnownCacheUrl = response?.cacheServers?.firstOrNull()
        appConfigStore.revertCacheUrlOverrideFlag()
        appConfigStore.updateConfig {
            copy(cacheUrl = wellKnownCacheUrl ?: DEFAULT_APP_CONFIG.cacheUrl)
        }
    }
}
