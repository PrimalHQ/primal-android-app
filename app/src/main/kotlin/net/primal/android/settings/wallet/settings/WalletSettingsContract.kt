package net.primal.android.settings.wallet.settings

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.connections.primal.model.PrimalNwcConnectionInfo
import net.primal.domain.links.CdnImage
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.Wallet

interface WalletSettingsContract {
    data class UiState(
        val activeWallet: Wallet? = null,
        val useExternalWallet: Boolean? = null,
        val nwcConnectionsInfo: List<PrimalNwcConnectionInfo> = emptyList(),
        val connectionsState: ConnectionsState = ConnectionsState.Loading,
        val showBackupWidget: Boolean = false,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val activeAccountDisplayName: String = "",
        val isExportingTransactions: Boolean = false,
        val transactions: List<Transaction> = emptyList(),
    )

    sealed class UiEvent {
        data object DisconnectWallet : UiEvent()
        data object RequestFetchWalletConnections : UiEvent()
        data object ExportTransactions : UiEvent()
        data object TransactionsExported : UiEvent()
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
