package net.primal.android.settings.wallet

import net.primal.android.user.domain.NostrWallet

interface WalletContract {
    data class UiState(
        val wallet: NostrWallet? = null,
        val userLightningAddress: String? = null,
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
    }
}
