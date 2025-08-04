package net.primal.android.settings.wallet.settings

import net.primal.android.settings.wallet.domain.NwcConnectionInfo
import net.primal.domain.wallet.Wallet

interface WalletSettingsContract {
    data class UiState(
        val wallet: Wallet? = null,
        val preferPrimalWallet: Boolean? = null,
        val nwcConnectionsInfo: List<NwcConnectionInfo> = emptyList(),
        val connectionsState: ConnectionsState = ConnectionsState.Loading,
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
        data object RequestFetchWalletConnections : UiEvent()
        data class RevokeConnection(val nwcPubkey: String) : UiEvent()
        data class UpdatePreferPrimalWallet(val value: Boolean) : UiEvent()
        data class UpdateMinTransactionAmount(val amountInSats: Long) : UiEvent()
        data class ConnectExternalWallet(val connectionLink: String) : UiEvent()
    }

    enum class ConnectionsState {
        Loading,
        Error,
        Loaded,
    }
}
