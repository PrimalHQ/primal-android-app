package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseMembershipRequest(
    val name: String,
    @SerialName("onchain") val onChain: Boolean?,
    @SerialName("receiver_pubkey") val receiverUserId: String,
    @SerialName("amount_usd") val amountUsd: String? = null,
    @SerialName("product_id") val primalProductId: String? = null,
    @SerialName("android_subscription") val playSubscription: String? = null,
)
