package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.Serializable

@Serializable
data class GetBalanceResponse(
    val balance: Long,
)
