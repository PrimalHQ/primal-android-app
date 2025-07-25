package net.primal.android.premium.home

import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

interface PremiumHomeContract {

    data class UiState(
        val profileId: String? = null,
        val avatarCdnImage: CdnImage? = null,
        val profileNostrAddress: String? = null,
        val profileLightningAddress: String? = null,
        val membership: PremiumMembership? = null,
        val showSupportUsNotice: Boolean = false,
        val avatarLegendaryCustomization: LegendaryCustomization? = null,
        val error: MembershipError? = null,
    )

    sealed class UiEvent {
        data object DismissError : UiEvent()
        data object ApplyPrimalNostrAddress : UiEvent()
        data object ApplyPrimalLightningAddress : UiEvent()
        data object RequestMembershipUpdate : UiEvent()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onRenewSubscription: (primalName: String) -> Unit,
        val onManagePremium: () -> Unit,
        val onLegendCardClick: (String) -> Unit,
        val onSupportPrimal: () -> Unit,
        val onUpgradeToProClick: () -> Unit,
        val onContributePrimal: () -> Unit,
    )
}
