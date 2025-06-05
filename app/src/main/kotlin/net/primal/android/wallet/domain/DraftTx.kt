package net.primal.android.wallet.domain

import kotlinx.serialization.Serializable
import net.primal.android.wallet.api.model.LnInvoiceData

@Serializable
data class DraftTx(
    val status: DraftTxStatus = DraftTxStatus.Draft,
    val minSendable: String? = null,
    val maxSendable: String? = null,
    val targetLud16: String? = null,
    val targetLnUrl: String? = null,
    val targetOnChainAddress: String? = null,
    val targetUserId: String? = null,
    val onChainInvoice: String? = null,
    val lnInvoice: String? = null,
    val lnInvoiceData: LnInvoiceData? = null,
    val amountSats: String = "0",
    val noteRecipient: String? = null,
    val noteSelf: String? = null,
) {
    fun isLightningTx() = targetOnChainAddress == null
    fun isBtcTx() = targetOnChainAddress != null
    fun isLnInvoiceAmountless() =
        lnInvoice != null && (lnInvoiceData?.amountMilliSats == null || lnInvoiceData.amountMilliSats == 0)
}
