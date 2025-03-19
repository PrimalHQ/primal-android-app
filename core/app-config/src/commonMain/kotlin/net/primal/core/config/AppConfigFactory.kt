package net.primal.core.config

import androidx.datastore.core.DataStore
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.UserAgent
import net.primal.core.config.api.WellKnownApi
import net.primal.core.config.store.AppConfigDataStore
import net.primal.core.config.store.createAppConfigDataStorePersistence
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.coroutines.DispatcherProvider
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

    private val httpClient = HttpClientFactory.createHttpClientWithDefaultConfig {
        install(UserAgent) {
            agent = "Primal-Android"
        }
    }

    private val ktorfit = Ktorfit.Builder()
        .baseUrl("https://primal.net/")
        .httpClient(client = httpClient)
        .build()


    // TODO Fix this once build is success
    private val wellKnownApi: WellKnownApi by lazy {
        throw NotImplementedError()
//        ktorfit.createWellKnownApi()
    }

    private val persistence: DataStore<AppConfig> by lazy {
        createAppConfigDataStorePersistence("app-config.json")
    }

    private var appConfigDataStore: AppConfigDataStore? = null

    private fun getOrCreateAppConfigDataStore(
        dispatcherProvider: DispatcherProvider,
    ): AppConfigDataStore {
        return appConfigDataStore ?: AppConfigDataStore(
            dispatcherProvider = dispatcherProvider,
            persistence = persistence,
        ).also { appConfigDataStore = it }
    }

    fun createAppConfigProvider(
        dispatcherProvider: DispatcherProvider,
    ): AppConfigProvider {
        return DynamicAppConfigProvider(
            appConfigStore = getOrCreateAppConfigDataStore(dispatcherProvider),
            dispatcherProvider = dispatcherProvider,
        )
    }

    fun createAppConfigHandler(
        dispatcherProvider: DispatcherProvider,
    ): AppConfigHandler {
        return AppConfigHandlerImpl(
            dispatcherProvider = dispatcherProvider,
            appConfigStore = getOrCreateAppConfigDataStore(dispatcherProvider),
            wellKnownApi = wellKnownApi,
        )
    }
}
