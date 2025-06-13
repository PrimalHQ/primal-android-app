package net.primal.domain.wallet

data class LnInvoiceCreateResult(
    val invoice: String,
    val description: String? = null,
)
