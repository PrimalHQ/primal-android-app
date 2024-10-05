package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalZapEvent(
    @SerialName("event_id") val eventId: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("sender") val senderId: String,
    @SerialName("receiver") val receiverId: String,
    @SerialName("amount_sats") val amountSats: ULong,
    @SerialName("zap_receipt_id") val zapReceiptId: String,
)
