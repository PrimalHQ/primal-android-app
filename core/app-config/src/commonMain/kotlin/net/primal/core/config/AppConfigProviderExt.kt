package net.primal.core.config

import kotlinx.coroutines.flow.StateFlow
import net.primal.domain.global.PrimalServerType

suspend fun AppConfigProvider.observeApiUrlByType(type: PrimalServerType): StateFlow<String> {
    return when (type) {
        PrimalServerType.Caching -> cacheUrl()
        PrimalServerType.Upload -> uploadUrl()
        PrimalServerType.Wallet -> walletUrl()
    }
}
