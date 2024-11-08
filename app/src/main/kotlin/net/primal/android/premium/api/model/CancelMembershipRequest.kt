package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelMembershipRequest(
    @SerialName("android_subscription") val playSubscription: String? = null,
)
