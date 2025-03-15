package net.primal.core.networking.http

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpClientEngine(): HttpClientEngineFactory<*> = OkHttp
