package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MembershipPurchaseMonitorResponse(
    @SerialName("id") val id: String?,
    @SerialName("specification") val specification: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("requester_pubkey") val requesterPubkey: String?,
    @SerialName("completed_at") val completedAt: String?,
    @SerialName("receiver_pubkey") val receiverPubkey: String?,
)
