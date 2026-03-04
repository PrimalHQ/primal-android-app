package net.primal.android.wallet.notice.sheet

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

interface WalletNoticeSheetContract {
    data class UiState(
        val noticeType: WalletNoticeType? = null,
        val shouldShowNotice: Boolean = false,
        val activeUserCdnImage: CdnImage? = null,
        val activeUserLegendaryCustomization: LegendaryCustomization? = null,
    )

    sealed class UiEvent {
        data object DismissSheet : UiEvent()
    }
}

enum class WalletNoticeType {
    UpgradeWallet,
    WalletDiscontinued,
    WalletDetected,
}
