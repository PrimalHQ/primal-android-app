package net.primal.core.networking.nwc.model

import kotlinx.serialization.Serializable

@Serializable
data class NwcWalletRequest<T>(
    val method: String,
    val params: T,
)
