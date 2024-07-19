package net.primal.android.articles.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.withContext
import net.primal.android.articles.api.ArticlesApi
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.articles.db.Article
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase

@OptIn(ExperimentalPagingApi::class)
class ArticleFeedMediator(
    private val userId: String,
    private val articlesApi: ArticlesApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) : RemoteMediator<Int, Article>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Article>): MediatorResult {
        withContext(dispatcherProvider.io()) {
            val pageSize = state.config.pageSize
            val response = articlesApi.getArticleFeed(
                body = ArticleFeedRequestBody(
                    userId = userId,
                    feedUserId = userId,
                    limit = pageSize,
                ),
            )

            response.persistToDatabaseAsTransaction(
                userId = userId,
                database = database,
            )
        }
        return MediatorResult.Success(endOfPaginationReached = true)
    }
}
