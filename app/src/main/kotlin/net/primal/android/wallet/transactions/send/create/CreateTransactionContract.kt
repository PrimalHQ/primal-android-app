package net.primal.android.wallet.transactions.send.create

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.wallet.dashboard.CurrencyMode
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi

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
    ) {
        fun isNotInvoice() = transaction.lnInvoice == null && transaction.onChainInvoice == null
    }

    sealed class UiEvent {
        data class AmountChangedSats(val amountInSats: String) : UiEvent()
        data class AmountChangedFiat(val amountInUsd: String) : UiEvent()
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
