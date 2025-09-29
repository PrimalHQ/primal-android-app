package net.primal.data.remote.factory

import de.jensklingenberg.ktorfit.Ktorfit
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.data.remote.api.lightning.LightningApi
import net.primal.data.remote.api.lightning.createLightningApi

object NwcApiServiceFactory {

    private val defaultHttpClient = HttpClientFactory.createHttpClientWithDefaultConfig()

    fun createLightningApi(): LightningApi =
        Ktorfit.Builder()
            .httpClient(client = defaultHttpClient)
            .build()
            .createLightningApi()
}
