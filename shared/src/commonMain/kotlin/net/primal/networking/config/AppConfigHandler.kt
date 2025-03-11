package net.primal.networking.config

import io.github.aakira.napier.Napier
import kotlin.time.Duration
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.primal.core.coroutines.DispatcherProvider
import net.primal.networking.config.api.ApiConfigResponse
import net.primal.networking.config.api.WellKnownApi
import net.primal.networking.config.domain.DEFAULT_APP_CONFIG
import net.primal.networking.config.store.AppConfigDataStore

internal class AppConfigHandler(
    private val dispatcherProvider: DispatcherProvider,
    private val appConfigStore: AppConfigDataStore,
    private val wellKnownApi: WellKnownApi,
) {

    private var lastTimeFetched: Instant = Instant.DISTANT_PAST

    private fun isAppConfigSyncedInLast(duration: Duration): Boolean {
        return lastTimeFetched < Clock.System.now().minus(duration)
    }

    suspend fun updateAppConfigOrFailSilently() {
        val response = fetchAppConfigOrNull() ?: return
        lastTimeFetched = Clock.System.now()
        appConfigStore.updateConfig {
            copy(
                cacheUrl = response.cacheServers.firstOrNull() ?: this.cacheUrl,
                uploadUrl = response.uploadServers.firstOrNull() ?: this.uploadUrl,
                walletUrl = response.walletServers.firstOrNull() ?: this.walletUrl,
            )
        }
    }

    suspend fun updateAppConfigWithDebounce(duration: Duration) {
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

    suspend fun overrideCacheUrl(url: String) = appConfigStore.overrideCacheUrl(url = url)

    suspend fun restoreDefaultCacheUrl() {
        val response = fetchAppConfigOrNull()
        val wellKnownCacheUrl = response?.cacheServers?.firstOrNull()
        appConfigStore.revertCacheUrlOverrideFlag()
        appConfigStore.updateConfig {
            copy(cacheUrl = wellKnownCacheUrl ?: DEFAULT_APP_CONFIG.cacheUrl)
        }
    }
}
