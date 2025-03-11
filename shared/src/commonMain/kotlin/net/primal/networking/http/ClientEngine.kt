package net.primal.networking.http

import io.ktor.client.engine.HttpClientEngineFactory

expect fun createHttpClientEngine(): HttpClientEngineFactory<*>
