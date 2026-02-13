package net.primal.domain.wallet

import net.primal.domain.transactions.Transaction

data class TransactionsPage(
    val transactions: List<Transaction>,
    /**
     * Cursor for fetching the next page (older transactions).
     * For Primal wallet: paging.sinceId (oldest createdAt in current page)
     * Null when there are no more pages.
     */
    val nextCursor: Long?,
)
