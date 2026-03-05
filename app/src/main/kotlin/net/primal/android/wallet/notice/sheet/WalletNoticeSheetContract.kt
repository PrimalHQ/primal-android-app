package net.primal.android.wallet.notice.sheet

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

interface WalletNoticeSheetContract {
    data class UiState(
        val noticeType: WalletNoticeType? = null,
        val shouldShowNotice: Boolean = false,
        val creatingWallet: Boolean = false,
        val error: WalletCreationError? = null,
        val activeUserCdnImage: CdnImage? = null,
        val activeUserLegendaryCustomization: LegendaryCustomization? = null,
    ) {
        sealed class WalletCreationError {
            data class Failed(val cause: Throwable) : WalletCreationError()
        }
    }

    sealed class UiEvent {
        data object DismissSheet : UiEvent()
        data object CreateWallet : UiEvent()
        data object DismissError : UiEvent()
    }
}

enum class WalletNoticeType {
    UpgradeWallet,
    WalletDiscontinued,
    WalletDetected,
}
