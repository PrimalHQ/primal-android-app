package net.primal.android.wallet.dashboard

import net.primal.android.attachments.domain.CdnImage

interface WalletDashboardContract {
    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
    )
}
