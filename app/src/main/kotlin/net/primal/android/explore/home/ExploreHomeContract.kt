package net.primal.android.explore.home

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.domain.links.CdnImage

interface ExploreHomeContract {
    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val activeAccountPubkey: String? = null,
        val badges: Badges = Badges(),
    )

    data class ScreenCallbacks(
        val onDrawerQrCodeClick: () -> Unit,
        val onSearchClick: () -> Unit,
        val onAdvancedSearchClick: () -> Unit,
        val onFollowPackClick: (profileId: String, identifier: String) -> Unit,
        val onGoToWallet: () -> Unit,
        val onNewPostClick: () -> Unit,
    )
}
