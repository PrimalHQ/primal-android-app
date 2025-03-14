package net.primal.android.wallet.dashboard

import androidx.paging.PagingData
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import net.primal.android.events.domain.CdnImage
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.transactions.list.TransactionListItemDataUi

interface WalletDashboardContract {
    data class UiState(
        val transactions: Flow<PagingData<TransactionListItemDataUi>>,
        val isNpubLogin: Boolean = false,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val badges: Badges = Badges(),
        val primalWallet: PrimalWallet? = null,
        val walletPreference: WalletPreference = WalletPreference.Undefined,
        val walletBalance: BigDecimal? = null,
        val exchangeBtcUsdRate: Double? = null,
        val lastWalletUpdatedAt: Long? = null,
        val lowBalance: Boolean = false,
        val error: DashboardError? = null,
    ) {
        sealed class DashboardError {
            data class InAppPurchaseConfirmationFailed(val cause: Throwable) : DashboardError()
            data class InAppPurchaseNoticeError(val message: String?) : DashboardError()
        }
    }

    sealed class UiEvent {
        data object DismissError : UiEvent()
        data object EnablePrimalWallet : UiEvent()
    }
}
