package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentProfilePremiumInfo(
    @SerialName("cohort_1") val cohort1: String? = null,
    @SerialName("cohort_2") val cohort2: String? = null,
    @SerialName("tier") val tier: String? = null,
    @SerialName("expires_on") val expiresAt: Long? = null,
    @SerialName("legend_since") val legendSince: Long? = null,
    @SerialName("premium_since") val premiumSince: Long? = null,
)
