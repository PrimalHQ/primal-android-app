package net.primal.domain.repository

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.error.NetworkException
import net.primal.domain.model.Article
import net.primal.domain.model.FeedPost

interface ArticleRepository {

    fun feedBySpec(userId: String, feedSpec: String): Flow<PagingData<Article>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchArticleAndComments(
        userId: String,
        articleId: String,
        articleAuthorId: String,
    )

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchArticleHighlights(
        userId: String,
        articleId: String,
        articleAuthorId: String,
    )

    suspend fun observeArticle(articleId: String, articleAuthorId: String): Flow<Article>

    suspend fun observeArticleByEventId(eventId: String, articleAuthorId: String): Flow<Article>

    suspend fun observeArticleComments(
        userId: String,
        articleId: String,
        articleAuthorId: String,
    ): Flow<List<FeedPost>>

    suspend fun observeArticleByCommentId(commentNoteId: String): Flow<Article?>
}
