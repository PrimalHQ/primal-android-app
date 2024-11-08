package net.primal.android.premium.domain

import kotlinx.datetime.Clock
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
    val expiresOn: Long,
    val cohort1: String,
    val cohort2: String,
    val recurring: Boolean = false,
    val renewsOn: Long? = null,
) {
    fun isExpired() = Clock.System.now().epochSeconds > expiresOn
}
