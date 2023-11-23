package net.primal.android.config.dynamic

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.config.api.ApiConfigResponse
import net.primal.android.config.api.WellKnownApi
import net.primal.android.config.store.AppConfigDataStore

@Singleton
class AppConfigUpdater @Inject constructor(
    private val appConfigStore: AppConfigDataStore,
    private val wellKnownApi: WellKnownApi,
) {

    suspend fun fetchAndStoreLatestAppConfig() {
        val response = fetchAppConfigOrNull() ?: return
        appConfigStore.updateConfig {
            copy(
                cacheUrl = response.cacheServers.firstOrNull() ?: this.cacheUrl,
                uploadUrl = response.uploadServers.firstOrNull() ?: this.uploadUrl,
                walletUrl = response.walletServers.firstOrNull() ?: this.walletUrl,

            )
        }
    }

    private suspend fun fetchAppConfigOrNull(): ApiConfigResponse? {
        val result = runCatching {
            withContext(Dispatchers.IO) { wellKnownApi.fetchApiConfig() }
        }
        return result.getOrNull()
    }
}
