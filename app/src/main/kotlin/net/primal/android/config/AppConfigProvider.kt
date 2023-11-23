package net.primal.android.config

import kotlinx.coroutines.flow.StateFlow

interface AppConfigProvider {
    suspend fun cacheUrl(): StateFlow<String>
    suspend fun uploadUrl(): StateFlow<String>
    suspend fun walletUrl(): StateFlow<String>
}
