package net.primal.core.config

import kotlin.time.Duration

interface AppConfigHandler {

    suspend fun updateAppConfigOrFailSilently()

    suspend fun updateAppConfigWithDebounce(duration: Duration)

    suspend fun overrideCacheUrl(url: String)

    suspend fun restoreDefaultCacheUrl()
}
