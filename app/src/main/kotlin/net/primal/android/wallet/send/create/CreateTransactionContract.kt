package net.primal.android.wallet.send.create

import net.primal.android.attachments.domain.CdnImage

interface CreateTransactionContract {

    data class UiState(
        val transaction: DraftTransaction,
        val error: Throwable? = null,
        val profileAvatarCdnImage: CdnImage? = null,
        val profileDisplayName: String? = null,
        val profileLightningAddress: String? = null,
    )

    sealed class UiEvent {
        data class AmountChanged(val amountInSats: String) : UiEvent()
        data class SendTransaction(val note: String?) : UiEvent()
    }
}
