package net.primal.android.wallet.transactions.send.create

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi
import net.primal.domain.links.CdnImage
import net.primal.domain.wallet.CurrencyMode
import net.primal.domain.wallet.DraftTx
import net.primal.domain.wallet.DraftTxStatus
import net.primal.domain.wallet.Wallet

interface CreateTransactionContract {

    data class UiState(
        val transaction: DraftTx,
        val activeWallet: Wallet? = null,
        val parsingInvoice: Boolean = false,
        val fetchingMiningFees: Boolean = false,
        val miningFeeTiers: List<MiningFeeUi> = emptyList(),
        val selectedFeeTierIndex: Int? = null,
        val error: Throwable? = null,
        val profileAvatarCdnImage: CdnImage? = null,
        val profileDisplayName: String? = null,
        val profileLightningAddress: String? = null,
        val profileLegendaryCustomization: LegendaryCustomization? = null,
        val currencyMode: CurrencyMode = CurrencyMode.SATS,
        val amountInUsd: String = "0",
        val currentExchangeRate: Double? = null,
        val maximumUsdAmount: BigDecimal? = null,
        val sendingCompleted: Boolean = false,
        val pendingFinalStatus: DraftTxStatus? = null,
    ) {
        fun isNotInvoice() = transaction.lnInvoice == null && transaction.onChainInvoice == null
    }

    sealed class UiEvent {
        data class AmountChanged(val amount: String) : UiEvent()
        data class ChangeCurrencyMode(val currencyMode: CurrencyMode) : UiEvent()
        data object AmountApplied : UiEvent()
        data class MiningFeeChanged(val tierId: String) : UiEvent()
        data object ReloadMiningFees : UiEvent()
        data class SendTransaction(
            val noteRecipient: String?,
            val noteSelf: String?,
            val miningFeeTierId: String? = null,
        ) : UiEvent()

        data object SendingAnimationFinished : UiEvent()
    }
}
