package net.primal.android.navigation.interactions

interface ArticleInteractionCallbacks {
    fun onArticleClick(naddr: String)
    fun onArticleReplyClick(naddr: String)
    fun onArticleQuoteClick(naddr: String)
    fun onHighlightReplyClick(highlightNevent: String, articleNaddr: String)
    fun onHighlightQuoteClick(highlightNevent: String, articleNaddr: String)
}
