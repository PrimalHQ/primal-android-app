package net.primal.android.networking.primal

import kotlinx.coroutines.flow.StateFlow
import net.primal.android.config.AppConfigProvider

suspend fun AppConfigProvider.observeApiUrlByType(type: PrimalServerType): StateFlow<String> {
    return when (type) {
        PrimalServerType.Caching -> cacheUrl()
        PrimalServerType.Upload -> uploadUrl()
        PrimalServerType.Wallet -> walletUrl()
    }
}
