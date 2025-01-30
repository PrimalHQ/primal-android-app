package net.primal.android.settings.wallet

import net.primal.android.settings.wallet.model.NwcConnectionInfo
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.WalletPreference

interface WalletSettingsContract {
    data class UiState(
        val wallet: NostrWalletConnect? = null,
        val walletPreference: WalletPreference = WalletPreference.Undefined,
        val userLightningAddress: String? = null,
        val maxWalletBalanceInBtc: String? = null,
        val spamThresholdAmountInSats: Long? = null,
        val nwcConnectionsInfo: List<NwcConnectionInfo> = emptyList(),
        val connectionsState: ConnectionsState = ConnectionsState.Loading,
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
        data object RetryConnectionsFetch : UiEvent()
        data object RequestFetchWalletConnections : UiEvent()
        data class RevokeConnection(val nwcPubkey: String) : UiEvent()
        data class UpdateWalletPreference(val walletPreference: WalletPreference) : UiEvent()
        data class UpdateMinTransactionAmount(val amountInSats: Long) : UiEvent()
    }

    enum class ConnectionsState {
        Loading,
        Error,
        Loaded,
    }
}
