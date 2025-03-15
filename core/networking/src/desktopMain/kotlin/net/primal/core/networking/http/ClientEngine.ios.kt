package net.primal.core.networking.http

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun createHttpClientEngine(): HttpClientEngineFactory<*> = CIO
