package net.primal.core.networking.http

import io.ktor.client.engine.HttpClientEngineFactory

expect fun createHttpClientEngine(): HttpClientEngineFactory<*>
