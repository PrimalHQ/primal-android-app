package net.primal.networking.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClientEngine(): HttpClientEngineFactory<*> = Darwin
