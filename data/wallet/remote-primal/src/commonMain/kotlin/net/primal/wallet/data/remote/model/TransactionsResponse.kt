package net.primal.wallet.data.remote.model

import net.primal.domain.common.ContentPrimalPaging
import net.primal.wallet.data.remote.nostr.ContentWalletTransaction

data class TransactionsResponse(
    val transactions: List<ContentWalletTransaction>,
    val paging: ContentPrimalPaging? = null,
)
