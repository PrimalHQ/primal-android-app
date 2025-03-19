package net.primal.data.local.dao.profiles

import net.primal.domain.plus
import net.primal.domain.utils.isPrimalLegendTier

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
