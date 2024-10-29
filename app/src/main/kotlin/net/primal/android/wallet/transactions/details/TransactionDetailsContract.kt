package net.primal.android.wallet.transactions.details

import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.notes.feed.model.FeedPostUi

interface TransactionDetailsContract {
    data class UiState(
        val loading: Boolean = false,
        val txData: TransactionDetailDataUi? = null,
        val feedPost: FeedPostUi? = null,
        val articlePost: FeedArticleUi? = null,
    )
}
