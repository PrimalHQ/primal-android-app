package net.primal.android.settings.wallet.settings

import net.primal.domain.connections.primal.model.PrimalNwcConnectionInfo
import net.primal.domain.wallet.Wallet

interface WalletSettingsContract {
    data class UiState(
        val activeWallet: Wallet? = null,
        val useExternalWallet: Boolean? = null,
        val nwcConnectionsInfo: List<PrimalNwcConnectionInfo> = emptyList(),
        val connectionsState: ConnectionsState = ConnectionsState.Loading,
        val showBackupWidget: Boolean = false,
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
        data object RequestFetchWalletConnections : UiEvent()
        data class RevokeConnection(val nwcPubkey: String) : UiEvent()
        data class UpdateUseExternalWallet(val value: Boolean) : UiEvent()
        data class UpdateMinTransactionAmount(val amountInSats: Long) : UiEvent()
        data class ConnectExternalWallet(val connectionLink: String) : UiEvent()
    }

    enum class ConnectionsState {
        Loading,
        Error,
        Loaded,
    }
}
