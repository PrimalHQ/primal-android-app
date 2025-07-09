package net.primal.android.navigation.interactions

data class ArticleInteractionCallbacks(
    val onArticleClick: (naddr: String) -> Unit,
    val onArticleReplyClick: (naddr: String) -> Unit,
    val onArticleQuoteClick: (naddr: String) -> Unit,
    val onHighlightReplyClick: (highlightNevent: String, articleNaddr: String) -> Unit,
    val onHighlightQuoteClick: (highlightNevent: String, articleNaddr: String) -> Unit,
)
