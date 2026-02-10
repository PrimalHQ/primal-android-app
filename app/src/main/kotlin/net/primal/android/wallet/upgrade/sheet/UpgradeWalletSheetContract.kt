package net.primal.android.wallet.upgrade.sheet

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

interface UpgradeWalletSheetContract {
    data class UiState(
        val shouldUserUpgrade: Boolean = false,
        val shouldShowNotice: Boolean = false,
        val activeUserCdnImage: CdnImage? = null,
        val activeUserLegendaryCustomization: LegendaryCustomization? = null,
    )

    sealed class UiEvent {
        data object DismissSheet : UiEvent()
    }
}
