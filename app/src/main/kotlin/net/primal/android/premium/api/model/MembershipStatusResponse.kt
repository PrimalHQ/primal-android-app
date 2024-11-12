package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MembershipStatusResponse(
    @SerialName("pubkey") val pubkey: String,
    @SerialName("tier") val tier: String,
    @SerialName("name") val name: String,
    @SerialName("nostr_address") val nostrAddress: String,
    @SerialName("lightning_address") val lightningAddress: String,
    @SerialName("primal_vip_profile") val primalVipProfile: String,
    @SerialName("used_storage") val usedStorage: Long,
    @SerialName("max_storage") val maxStorage: Long,
    @SerialName("cohort_1") val cohort1: String,
    @SerialName("cohort_2") val cohort2: String,
    @SerialName("expires_on") val expiresOn: Long? = null,
    @SerialName("recurring") val recurring: Boolean = false,
    @SerialName("renews_on") val renewsOn: Long? = null,
)
