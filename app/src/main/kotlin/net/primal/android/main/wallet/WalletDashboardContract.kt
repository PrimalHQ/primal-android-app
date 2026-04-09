package net.primal.android.main.wallet

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.wallet.transactions.list.TransactionListItemDataUi
import net.primal.domain.links.CdnImage
import net.primal.domain.wallet.Wallet

interface WalletDashboardContract {
    data class UiState(
        val transactions: Flow<PagingData<TransactionListItemDataUi>> = emptyFlow(),
        val refreshing: Boolean = false,
        val dashboardState: WalletDashboardState = WalletDashboardState.Loading,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val wallet: Wallet? = null,
        val exchangeBtcUsdRate: Double? = null,
        val walletPickerEnabled: Boolean = false,
        val lowBalance: Boolean = false,
        val isWalletBackedUp: Boolean = false,
        val hasPersistedSparkWallet: Boolean = false,
        val error: DashboardError? = null,
    ) {
        sealed class DashboardError {
            data class InAppPurchaseConfirmationFailed(val cause: Throwable) : DashboardError()
            data class InAppPurchaseNoticeError(val message: String?) : DashboardError()
            data class WalletCreationFailed(val cause: Throwable) : DashboardError()
        }
    }

    sealed class UiEvent {
        data object RequestWalletBalanceUpdate : UiEvent()
        data object RequestLatestTransactionsSync : UiEvent()
        data object EnrichTransactions : UiEvent()
        data object DismissError : UiEvent()
        data object CreateWallet : UiEvent()
    }

    enum class WalletDashboardState {
        Loading,
        ActiveWallet,
        WalletDetected,
        WalletDiscontinued,
        NoWallet,
        NoWalletNpubLogin,
    }
}
