package net.primal.core.networking.nwc

import net.primal.core.networking.factory.HttpClientFactory

object NwcNetworking {
    val httpClient by lazy { HttpClientFactory.createHttpClientWithDefaultConfig() }
}
