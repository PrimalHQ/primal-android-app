package net.primal.android.wallet.transactions.details

import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.domain.links.ReferencedStream

interface TransactionDetailsContract {
    data class UiState(
        val loading: Boolean = false,
        val txData: TransactionDetailDataUi? = null,
        val feedPost: FeedPostUi? = null,
        val articlePost: FeedArticleUi? = null,
        val referencedStream: ReferencedStream? = null,
        val currentExchangeRate: Double? = null,
    )
}
