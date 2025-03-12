package net.primal.db.profiles

import kotlinx.serialization.Serializable

@Serializable
data class PrimalPremiumInfo(
    val primalName: String? = null,
    val cohort1: String? = null,
    val cohort2: String? = null,
    val tier: String? = null,
    val expiresAt: Long? = null,
    val legendSince: Long? = null,
    val premiumSince: Long? = null,
    val legendProfile: PrimalLegendProfile? = null,
)
