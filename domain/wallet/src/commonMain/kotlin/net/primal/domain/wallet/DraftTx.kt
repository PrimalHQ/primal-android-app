package net.primal.domain.wallet

import kotlinx.serialization.Serializable

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
    val onChainMiningFeeTier: String? = null,
    val lnInvoice: String? = null,
    var lnInvoiceAmountMilliSats: Int? = null,
    var lnInvoiceDescription: String? = null,
    val amountSats: String = "0",
    val noteRecipient: String? = null,
    val noteSelf: String? = null,
) {
    fun isLightningTx() = targetOnChainAddress == null
    fun isBtcTx() = targetOnChainAddress != null
    fun isLnInvoiceAmountless() =
        lnInvoice != null && (lnInvoiceAmountMilliSats == null || lnInvoiceAmountMilliSats == 0)
}
