package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetInfoResponsePayload(
    val alias: String,
    val color: String? = null,
    val pubkey: String? = null,
    val network: String? = null,
    @SerialName("block_height") val blockHeight: Long? = null,
    @SerialName("block_hash") val blockHash: String? = null,
    val methods: List<String> = emptyList(),
)
