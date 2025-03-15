package net.primal.core.networking.config

import kotlinx.coroutines.flow.StateFlow
import net.primal.core.networking.primal.PrimalServerType

suspend fun AppConfigProvider.observeApiUrlByType(type: PrimalServerType): StateFlow<String> {
    return when (type) {
        PrimalServerType.Caching -> cacheUrl()
        PrimalServerType.Upload -> uploadUrl()
        PrimalServerType.Wallet -> walletUrl()
    }
}
