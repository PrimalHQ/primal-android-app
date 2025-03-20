package net.primal.data.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.FeedApiImpl

object PrimalApiFactory {

    fun createFeedsApi(primalApiClient: PrimalApiClient): FeedApi = FeedApiImpl(primalApiClient)

//    factory<UploadApi> {
//        UploadApiSingleConnection(
//            primalUploadClient = PrimalApiClientFactory.create(PrimalServerType.Upload),
//        )
//    }
//
//    factory<PrimalImportApi> {
//        PrimalImportApiImpl(
//            primalApiClient = PrimalApiClientFactory.create(PrimalServerType.Caching),
//        )
//    }
}
