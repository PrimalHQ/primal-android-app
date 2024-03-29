package net.primal.android.settings.wallet

import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.WalletPreference

interface WalletSettingsContract {
    data class UiState(
        val wallet: NostrWalletConnect? = null,
        val walletPreference: WalletPreference = WalletPreference.Undefined,
        val userLightningAddress: String? = null,
        val maxWalletBalanceInBtc: String? = null,
        val spamThresholdAmountInSats: Long? = null,
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
        data class UpdateWalletPreference(val walletPreference: WalletPreference) : UiEvent()
        data class UpdateMinTransactionAmount(val amountInSats: Long) : UiEvent()
    }
}
