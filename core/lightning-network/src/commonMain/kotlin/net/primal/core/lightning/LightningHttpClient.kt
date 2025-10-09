package net.primal.core.lightning

import net.primal.core.networking.factory.HttpClientFactory

internal object LightningHttpClient {

    internal val defaultHttpClient = HttpClientFactory.createHttpClientWithDefaultConfig()
}
