package net.primal.android.explore.home

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.domain.CdnImage

interface ExploreHomeContract {
    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountPubkey: String? = null,
        val badges: Badges = Badges(),
    )
}
