package net.primal.android.premium.domain

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumMembership(
    val userId: String,
    val tier: String,
    val premiumName: String,
    val nostrAddress: String,
    val lightningAddress: String,
    val vipProfile: String,
    val usedStorageInBytes: Long,
    val maxStorageInBytes: Long,
    val cohort1: String,
    val cohort2: String,
    val expiresOn: Long? = null,
    val recurring: Boolean = false,
    val renewsOn: Long? = null,
    val origin: String? = null,
    val editedShoutout: String? = null,
) {
    fun isExpired() = expiresOn != null && Clock.System.now().epochSeconds > expiresOn
}
