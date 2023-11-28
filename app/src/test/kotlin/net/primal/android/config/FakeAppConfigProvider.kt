package net.primal.android.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import net.primal.android.config.domain.DEFAULT_APP_CONFIG

class FakeAppConfigProvider : AppConfigProvider {

    private val cacheStateFlow = MutableStateFlow(DEFAULT_APP_CONFIG.cacheUrl)
    fun setCacheUrl(url: String) = cacheStateFlow.getAndUpdate { url }

    private val uploadStateFlow = MutableStateFlow(DEFAULT_APP_CONFIG.uploadUrl)
    fun setUploadUrl(url: String) = uploadStateFlow.getAndUpdate { url }

    private val walletStateFlow = MutableStateFlow(DEFAULT_APP_CONFIG.walletUrl)
    fun setWalletUrl(url: String) = walletStateFlow.getAndUpdate { url }

    override suspend fun cacheUrl(): StateFlow<String> = cacheStateFlow

    override suspend fun uploadUrl(): StateFlow<String> = uploadStateFlow

    override suspend fun walletUrl(): StateFlow<String> = walletStateFlow
}
