package net.primal.domain

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

operator fun PrimalPremiumInfo?.plus(primalPremiumInfo: PrimalPremiumInfo?) =
    PrimalPremiumInfo(
        primalName = this?.primalName ?: primalPremiumInfo?.primalName,
        cohort1 = this?.cohort1 ?: primalPremiumInfo?.cohort1,
        cohort2 = this?.cohort2 ?: primalPremiumInfo?.cohort2,
        tier = this?.tier ?: primalPremiumInfo?.tier,
        expiresAt = this?.expiresAt ?: primalPremiumInfo?.expiresAt,
        legendSince = this?.legendSince ?: primalPremiumInfo?.legendSince,
        premiumSince = this?.premiumSince ?: primalPremiumInfo?.premiumSince,
        legendProfile = this?.legendProfile ?: primalPremiumInfo?.legendProfile,
    )
