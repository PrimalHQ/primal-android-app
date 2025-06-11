package net.primal.wallet.data.remote.factory

import net.primal.core.networking.factory.HttpClientFactory

object WalletApiServiceFactory {

    private val defaultHttpClient = HttpClientFactory.createHttpClientWithDefaultConfig()

//    fun createArticlesApi(primalApiClient: PrimalApiClient): ArticlesApi = ArticlesApiImpl(primalApiClient)
}
