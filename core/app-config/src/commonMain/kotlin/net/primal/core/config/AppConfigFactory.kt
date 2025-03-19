package net.primal.core.config

import androidx.datastore.core.DataStore
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.UserAgent
import net.primal.core.config.api.WellKnownApi
import net.primal.core.config.api.createWellKnownApi
import net.primal.core.config.store.AppConfigDataStore
import net.primal.core.config.store.createAppConfigDataStorePersistence
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.domain.AppConfig

private const val CONFIG_CACHE_API = "wss://cache1.primal.net/v1"
private const val CONFIG_UPLOAD_API = "wss://uploads.primal.net/v1"
private const val CONFIG_WALLET_API = "wss://wallet.primal.net/v1"

internal val DEFAULT_APP_CONFIG = AppConfig(
    cacheUrl = CONFIG_CACHE_API,
    uploadUrl = CONFIG_UPLOAD_API,
    walletUrl = CONFIG_WALLET_API,
)

object AppConfigFactory {

    private val dispatcherProvider by lazy { DispatcherProviderFactory.create() }

    private val httpClient = HttpClientFactory.createHttpClientWithDefaultConfig {
        install(UserAgent) {
            agent = "Primal-Android"
        }
    }

    private val wellKnownApi: WellKnownApi by lazy {
        Ktorfit.Builder()
            .baseUrl("https://primal.net/")
            .httpClient(client = httpClient)
            .build()
            .createWellKnownApi()
    }

    private val persistence: DataStore<AppConfig> by lazy {
        createAppConfigDataStorePersistence("app-config.json")
    }

    private val appConfigDataStore: AppConfigDataStore by lazy {
        AppConfigDataStore(
            dispatcherProvider = dispatcherProvider,
            persistence = persistence,
        )
    }

    fun createAppConfigProvider(): AppConfigProvider {
        return DynamicAppConfigProvider(
            appConfigStore = appConfigDataStore,
            dispatcherProvider = dispatcherProvider,
        )
    }

    fun createAppConfigHandler(): AppConfigHandler {
        return AppConfigHandlerImpl(
            dispatcherProvider = dispatcherProvider,
            appConfigStore = appConfigDataStore,
            wellKnownApi = wellKnownApi,
        )
    }
}
