package net.primal.android.wallet.transactions.details

import net.primal.android.core.compose.feed.model.FeedPostUi

interface TransactionDetailsContract {
    data class UiState(
        val loading: Boolean = false,
        val txData: TransactionDetailDataUi? = null,
        val feedPost: FeedPostUi? = null,
    )
}
