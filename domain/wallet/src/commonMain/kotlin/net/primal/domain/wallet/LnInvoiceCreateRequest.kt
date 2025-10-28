package net.primal.domain.wallet

data class LnInvoiceCreateRequest(
    val amountInBtc: String? = null,
    val description: String? = null,
    val descriptionHash: String? = null,
    val expiry: Long? = null,
)
