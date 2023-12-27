package net.primal.android.wallet.send.create

import kotlinx.serialization.Serializable
import net.primal.android.wallet.api.model.LnInvoiceData

@Serializable
data class DraftTransaction(
    val status: TransactionStatus = TransactionStatus.Draft,
    val minSendable: String? = null,
    val maxSendable: String? = null,
    val targetLud16: String? = null,
    val targetLnUrl: String? = null,
    val targetUserId: String? = null,
    val lnInvoice: String? = null,
    val lnInvoiceData: LnInvoiceData? = null,
    val amountSats: String = "0",
    val note: String? = null,
)
