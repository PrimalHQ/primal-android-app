package net.primal.android.premium.domain

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
)
