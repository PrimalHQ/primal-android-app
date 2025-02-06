package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseMembershipRequest(
    @SerialName("amount_usd") val amountUsd: String? = null,
    val name: String,
    @SerialName("receiver_pubkey") val receiverUserId: String,
    @SerialName("product_id") val primalProductId: String? = null,
    @SerialName("android_subscription") val playSubscription: String? = null,
    @SerialName("onchain") val onChain: Boolean?,
)
