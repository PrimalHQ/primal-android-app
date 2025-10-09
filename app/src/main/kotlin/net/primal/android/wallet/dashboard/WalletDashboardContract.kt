package net.primal.android.wallet.dashboard

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.android.wallet.transactions.list.TransactionListItemDataUi
import net.primal.domain.links.CdnImage
import net.primal.domain.wallet.Wallet

interface WalletDashboardContract {
    data class UiState(
        val transactions: Flow<PagingData<TransactionListItemDataUi>> = emptyFlow(),
        val isNpubLogin: Boolean = false,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val userWallets: List<Wallet> = emptyList(),
        val badges: Badges = Badges(),
        val wallet: Wallet? = null,
        val exchangeBtcUsdRate: Double? = null,
        val lowBalance: Boolean = false,
        val error: DashboardError? = null,
    ) {
        sealed class DashboardError {
            data class InAppPurchaseConfirmationFailed(val cause: Throwable) : DashboardError()
            data class InAppPurchaseNoticeError(val message: String?) : DashboardError()
        }
    }

    sealed class UiEvent {
        data object RequestWalletBalanceUpdate : UiEvent()
        data object DismissError : UiEvent()
        data object EnablePrimalWallet : UiEvent()
        data class ChangeActiveWallet(val wallet: Wallet) : UiEvent()
    }
}
