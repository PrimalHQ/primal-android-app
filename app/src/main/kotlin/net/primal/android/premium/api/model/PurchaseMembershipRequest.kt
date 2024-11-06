package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseMembershipRequest(
    val name: String,
    @SerialName("receiver_pubkey") val receiverUserId: String,
    @SerialName("product_id") val primalProductId: String? = null,
    @SerialName("android_subscription") val playSubscription: String? = null,
)
