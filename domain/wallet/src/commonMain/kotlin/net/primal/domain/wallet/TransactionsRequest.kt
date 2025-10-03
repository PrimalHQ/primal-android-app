package net.primal.domain.wallet

import net.primal.domain.nostr.InvoiceType

data class TransactionsRequest(
    val since: Long? = null,
    val until: Long? = null,
    val limit: Int? = null,
    val minAmountInBtc: String? = null,
    val unpaid: Boolean? = null,
    val type: InvoiceType? = null,
)
