package net.primal.android.articles.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.withContext
import net.primal.android.articles.api.ArticlesApi
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.articles.db.Article
import net.primal.android.articles.db.ArticleFeedCrossRef
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.primal.retryNetworkCall
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.mapNotNullAsArticleDataPO
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class ArticleFeedMediator(
    private val userId: String,
    private val feedSpec: String,
    private val articlesApi: ArticlesApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) : RemoteMediator<Int, Article>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Article>): MediatorResult {
        withContext(dispatcherProvider.io()) {
            val pageSize = state.config.pageSize
            val response = try {
                retryNetworkCall {
                    articlesApi.getArticleFeed(
                        body = ArticleFeedRequestBody(
                            spec = feedSpec,
                            userId = userId,
                            limit = pageSize,
                        ),
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
                null
            }

            val connections = response?.articles
                ?.mapNotNullAsArticleDataPO()
                ?.map {
                    ArticleFeedCrossRef(
                        spec = feedSpec,
                        articleId = it.articleId,
                        articleAuthorId = it.authorId,
                    )
                }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.articleFeedsConnections().deleteConnectionsBySpec(spec = feedSpec)
                }

                if (!connections.isNullOrEmpty()) {
                    database.articleFeedsConnections().connect(data = connections)
                }

                response?.persistToDatabaseAsTransaction(
                    userId = userId,
                    database = database,
                )
            }
        }

        return MediatorResult.Success(endOfPaginationReached = true)
    }
}
