package net.primal.data.local.dao.profiles

import net.primal.domain.premium.isPrimalLegendTier
import net.primal.domain.premium.plus

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
