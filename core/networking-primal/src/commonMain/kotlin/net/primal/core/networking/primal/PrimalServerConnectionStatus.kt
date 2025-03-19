package net.primal.core.networking.primal

import net.primal.domain.PrimalServerType

data class PrimalServerConnectionStatus(
    val serverType: PrimalServerType,
    val url: String = "",
    val connected: Boolean = false,
)
