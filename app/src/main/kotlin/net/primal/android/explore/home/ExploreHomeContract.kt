package net.primal.android.explore.home

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.user.domain.Badges

interface ExploreHomeContract {
    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryStyle: LegendaryStyle? = null,
        val activeAccountPubkey: String? = null,
        val badges: Badges = Badges(),
    )
}
