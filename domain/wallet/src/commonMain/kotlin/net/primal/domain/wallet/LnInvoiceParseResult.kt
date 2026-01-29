package net.primal.domain.wallet

data class LnInvoiceParseResult(
    val userId: String? = null,
    val amountMilliSats: Int? = null,
    val description: String? = null,
    val comment: String? = null,
    val date: Long? = null,
    val expiry: Long? = null,
    val paymentHash: String? = null,
)
