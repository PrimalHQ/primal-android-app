package net.primal.android.wallet.dashboard

import androidx.paging.PagingData
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.transactions.TransactionDataUi

interface WalletDashboardContract {
    data class UiState(
        val transactions: Flow<PagingData<TransactionDataUi>>,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val primalWallet: PrimalWallet? = null,
        val walletPreference: WalletPreference = WalletPreference.Undefined,
        val walletBalance: BigDecimal? = null,
        val error: DashboardError? = null,
    ) {
        sealed class DashboardError {
            data class InAppPurchaseConfirmationFailed(val cause: Throwable) : DashboardError()
        }
    }

    sealed class UiEvent {
        data class UpdateWalletPreference(val walletPreference: WalletPreference) : UiEvent()
        data object DismissError : UiEvent()
    }
}
