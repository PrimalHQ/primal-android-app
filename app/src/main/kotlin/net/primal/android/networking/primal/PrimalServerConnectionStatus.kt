package net.primal.android.networking.primal

data class PrimalServerConnectionStatus(
    val serverType: PrimalServerType,
    val url: String = "",
    val connected: Boolean = false,
)
