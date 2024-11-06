package net.primal.android.premium.user

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership

interface PremiumUserContract {

    data class UiState(
        val displayName: String = "",
        val avatarCdnImage: CdnImage? = null,
        val profileNostrAddress: String? = null,
        val profileLightningAddress: String? = null,
        val membership: PremiumMembership? = null,
    )
}
