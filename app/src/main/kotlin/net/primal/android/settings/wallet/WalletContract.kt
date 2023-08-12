package net.primal.android.settings.wallet

import net.primal.android.user.domain.NostrWalletConnect

interface WalletContract {
    data class UiState(
        val nwcUrl: String? = null,
        val isWalletConnected: Boolean = false,
        val nwc: NostrWalletConnect? = null
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
    }
}