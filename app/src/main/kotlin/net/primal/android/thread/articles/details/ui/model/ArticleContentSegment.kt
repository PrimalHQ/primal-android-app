package net.primal.android.thread.articles.details.ui.model

sealed class ArticleContentSegment {
    data class Text(val content: String) : ArticleContentSegment()
    data class Image(val imageUrl: String, val linkUrl: String? = null) : ArticleContentSegment()
}
