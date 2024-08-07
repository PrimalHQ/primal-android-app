package net.primal.android.config

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlinx.coroutines.withContext
import net.primal.android.config.api.ApiConfigResponse
import net.primal.android.config.api.WellKnownApi
import net.primal.android.config.domain.DEFAULT_APP_CONFIG
import net.primal.android.config.store.AppConfigDataStore
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import timber.log.Timber

@Singleton
class AppConfigHandler @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val appConfigStore: AppConfigDataStore,
    private val wellKnownApi: WellKnownApi,
) {

    private var lastTimeFetched: Instant = Instant.EPOCH

    private fun isAppConfigSyncedInLast(duration: Duration): Boolean {
        return lastTimeFetched < Instant.now().minusMillis(duration.inWholeMilliseconds)
    }

    suspend fun updateAppConfigOrFailSilently() {
        val response = fetchAppConfigOrNull() ?: return
        lastTimeFetched = Instant.now()
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
        result.exceptionOrNull()?.let { Timber.w(it) }
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
