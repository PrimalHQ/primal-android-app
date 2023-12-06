package net.primal.android.wallet.dashboard

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference

interface WalletDashboardContract {
    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val primalWallet: PrimalWallet? = null,
        val walletPreference: WalletPreference = WalletPreference.Undefined,
    )

    sealed class UiEvent {
        data class UpdateWalletPreference(val walletPreference: WalletPreference) : UiEvent()
    }
}
