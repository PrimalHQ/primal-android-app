package net.primal.core.lightning.factory

import de.jensklingenberg.ktorfit.Ktorfit
import net.primal.core.lightning.api.LightningApi
import net.primal.core.lightning.api.createLightningApi
import net.primal.core.networking.factory.HttpClientFactory

object LightningApiServiceFactory {

    internal val defaultHttpClient = HttpClientFactory.createHttpClientWithDefaultConfig()

    fun createLightningApi(): LightningApi =
        Ktorfit.Builder()
            .httpClient(client = defaultHttpClient)
            .build()
            .createLightningApi()
}
