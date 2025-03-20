package net.primal.data.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.articles.ArticlesApi
import net.primal.data.remote.api.articles.ArticlesApiImpl
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.FeedApiImpl
import net.primal.data.remote.api.importing.PrimalImportApi
import net.primal.data.remote.api.importing.PrimalImportApiImpl

object PrimalApiFactory {

    fun createArticlesApi(primalApiClient: PrimalApiClient): ArticlesApi = ArticlesApiImpl(primalApiClient)

    fun createFeedsApi(primalApiClient: PrimalApiClient): FeedApi = FeedApiImpl(primalApiClient)

    fun createImportApi(primalApiClient: PrimalApiClient): PrimalImportApi = PrimalImportApiImpl(primalApiClient)

}
