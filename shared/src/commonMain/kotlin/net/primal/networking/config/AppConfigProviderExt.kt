package net.primal.networking.config

import kotlinx.coroutines.flow.StateFlow
import net.primal.networking.primal.PrimalServerType

suspend fun AppConfigProvider.observeApiUrlByType(type: PrimalServerType): StateFlow<String> {
    return when (type) {
        PrimalServerType.Caching -> cacheUrl()
        PrimalServerType.Upload -> uploadUrl()
        PrimalServerType.Wallet -> walletUrl()
    }
}
