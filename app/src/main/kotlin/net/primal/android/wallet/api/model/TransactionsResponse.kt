package net.primal.android.wallet.api.model

import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
import net.primal.android.nostr.model.primal.content.ContentWalletTransaction

data class TransactionsResponse(
    val transactions: List<ContentWalletTransaction>,
    val paging: ContentPrimalPaging? = null,
)
