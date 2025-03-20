package net.primal.android.wallet.api.model

import net.primal.android.nostr.model.primal.content.ContentWalletTransaction
import net.primal.data.remote.model.ContentPrimalPaging

data class TransactionsResponse(
    val transactions: List<ContentWalletTransaction>,
    val paging: ContentPrimalPaging? = null,
)
