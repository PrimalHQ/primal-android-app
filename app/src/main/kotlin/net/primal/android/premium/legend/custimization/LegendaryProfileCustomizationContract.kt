package net.primal.android.premium.legend.custimization

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.premium.legend.LegendaryProfile

interface LegendaryProfileCustomizationContract {

    data class UiState(
        val avatarCdnImage: CdnImage? = null,
        val membership: PremiumMembership? = null,
        val customBadge: Boolean = false,
        val avatarGlow: Boolean = false,
        val legendaryProfile: LegendaryProfile = LegendaryProfile.NO_CUSTOMIZATION,
    )
}
