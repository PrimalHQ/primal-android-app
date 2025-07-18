package net.primal.wallet.data.model

import net.primal.domain.nostr.InvoiceType
import net.primal.domain.wallet.SubWallet

sealed class TransactionsRequest(
    open val since: Long? = null,
    open val until: Long? = null,
    open val limit: Int? = null,
) {
    data class Primal(
        override val since: Long? = null,
        override val until: Long? = null,
        override val limit: Int? = null,
        val subWallet: SubWallet,
        val minAmountInBtc: String? = null,
    ) : TransactionsRequest(since, until, limit)

    data class NWC(
        override val since: Long? = null,
        override val until: Long? = null,
        override val limit: Int? = null,
        val offset: Int? = null,
        val unpaid: Boolean? = null,
        val type: InvoiceType? = null,
    ) : TransactionsRequest(since, until, limit)
}
