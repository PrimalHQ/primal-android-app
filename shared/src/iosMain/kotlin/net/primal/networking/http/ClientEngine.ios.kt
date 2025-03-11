package net.primal.networking.http

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClientEngine(): HttpClientEngineFactory<*> = Darwin
