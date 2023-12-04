package net.primal.android.settings.wallet

import net.primal.android.user.domain.NostrWalletConnect

interface WalletSettingsContract {
    data class UiState(
        val wallet: NostrWalletConnect? = null,
        val userLightningAddress: String? = null,
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
    }
}
