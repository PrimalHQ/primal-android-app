package net.primal.android.premium.legend.custimization

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership

interface LegendaryProfileCustomizationContract {

    data class UiState(
        val avatarCdnImage: CdnImage? = null,
        val membership: PremiumMembership? = null,
    )
}
