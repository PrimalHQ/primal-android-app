package net.primal.android.wallet.transactions.send.create

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.wallet.domain.DraftTx

interface CreateTransactionContract {

    data class UiState(
        val transaction: DraftTx,
        val fetchingMiningFees: Boolean = false,
        val miningFeeTiers: List<MiningFeeUi> = emptyList(),
        val selectedFeeTierIndex: Int? = null,
        val minBtcTxAmountInSats: String? = null,
        val error: Throwable? = null,
        val profileAvatarCdnImage: CdnImage? = null,
        val profileDisplayName: String? = null,
        val profileLightningAddress: String? = null,
    ) {
        fun isNotInvoice() = transaction.lnInvoice == null && transaction.onChainInvoice == null
    }

    sealed class UiEvent {
        data class AmountChanged(val amountInSats: String) : UiEvent()
        data class SendTransaction(
            val noteRecipient: String?,
            val noteSelf: String?,
            val miningFeeTierId: String? = null,
        ) : UiEvent()
    }
}
