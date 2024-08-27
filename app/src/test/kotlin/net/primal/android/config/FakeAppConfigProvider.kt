package net.primal.android.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import net.primal.android.config.domain.DEFAULT_APP_CONFIG

class FakeAppConfigProvider(
    private val startDelay: Long? = null,
    private val delayDispatcher: CoroutineDispatcher? = null,
) : AppConfigProvider {

    private val cacheStateFlow = MutableStateFlow(DEFAULT_APP_CONFIG.cacheUrl)
    fun setCacheUrl(url: String) = cacheStateFlow.getAndUpdate { url }

    private val uploadStateFlow = MutableStateFlow(DEFAULT_APP_CONFIG.uploadUrl)
    fun setUploadUrl(url: String) = uploadStateFlow.getAndUpdate { url }

    private val walletStateFlow = MutableStateFlow(DEFAULT_APP_CONFIG.walletUrl)
    fun setWalletUrl(url: String) = walletStateFlow.getAndUpdate { url }

    override suspend fun cacheUrl(): StateFlow<String> =
        cacheStateFlow.let {
            if (startDelay != null && delayDispatcher != null) {
                it.onStart { delay(startDelay) }.stateIn(CoroutineScope(delayDispatcher))
            } else {
                it
            }
        }

    override suspend fun uploadUrl(): StateFlow<String> =
        uploadStateFlow.let {
            if (startDelay != null && delayDispatcher != null) {
                it.onStart { delay(startDelay) }.stateIn(CoroutineScope(delayDispatcher))
            } else {
                it
            }
        }

    override suspend fun walletUrl(): StateFlow<String> =
        walletStateFlow.let {
            if (startDelay != null && delayDispatcher != null) {
                it.onStart { delay(startDelay) }.stateIn(CoroutineScope(delayDispatcher))
            } else {
                it
            }
        }
}
