package net.primal.data.local.dao.profiles

import net.primal.domain.membership.isPrimalLegendTier
import net.primal.domain.membership.plus

fun ProfileData.combinePremiumInfoIfLegend(profileData: ProfileData?): ProfileData =
    if (this.primalPremiumInfo?.tier.isPrimalLegendTier() ||
        profileData?.primalPremiumInfo?.tier.isPrimalLegendTier()
    ) {
        copy(
            primalPremiumInfo = primalPremiumInfo + profileData?.primalPremiumInfo,
        )
    } else {
        this
    }
