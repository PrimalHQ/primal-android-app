package net.primal.android.profile.utils

import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.domain.PrimalPremiumInfo

fun ProfileData.combinePremiumInfoIfLegend(profileData: ProfileData?): ProfileData =
    if (this.primalPremiumInfo?.tier.isPrimalLegendTier() == true ||
        profileData?.primalPremiumInfo?.tier.isPrimalLegendTier() == true
    ) {
        copy(
            primalPremiumInfo = primalPremiumInfo + profileData?.primalPremiumInfo,
        )
    } else {
        this
    }

private operator fun PrimalPremiumInfo?.plus(primalPremiumInfo: PrimalPremiumInfo?) =
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
