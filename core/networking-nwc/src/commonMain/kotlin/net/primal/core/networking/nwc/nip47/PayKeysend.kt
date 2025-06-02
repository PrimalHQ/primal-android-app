package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TlvRecord(
    val type: Long,
    val value: String,
)

@Serializable
data class PayKeysendParams(
    val pubkey: String,
    val amount: Long,
    val preimage: String? = null,
    @SerialName("tlv_records") val tlvRecords: List<TlvRecord>? = null,
)

@Serializable
data class PayKeysendResponsePayload(
    val preimage: String,
)
