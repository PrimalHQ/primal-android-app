package net.primal.android.wallet.transactions.send.create

import java.math.BigDecimal
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi
import net.primal.android.wallet.utils.CurrencyMode

const val MAXIMUM_SATS = 99_999_990.00

interface CreateTransactionContract {

    data class UiState(
        val transaction: DraftTx,
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
    ) {
        fun isNotInvoice() = transaction.lnInvoice == null && transaction.onChainInvoice == null
    }

    sealed class UiEvent {
        data class AmountChanged(val amount: String, val currencyMode: CurrencyMode) : UiEvent()
        data class ChangeCurrencyMode(val currencyMode: CurrencyMode) : UiEvent()
        data object AmountApplied : UiEvent()
        data class MiningFeeChanged(val tierId: String) : UiEvent()
        data object ReloadMiningFees : UiEvent()
        data class SendTransaction(
            val noteRecipient: String?,
            val noteSelf: String?,
            val miningFeeTierId: String? = null,
        ) : UiEvent()
    }
}
