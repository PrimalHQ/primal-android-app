package net.primal.data.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.FeedApiImpl
import net.primal.data.remote.api.importing.PrimalImportApi
import net.primal.data.remote.api.importing.PrimalImportApiImpl
import net.primal.data.remote.api.upload.UploadApi

object PrimalApiFactory {

    fun createFeedsApi(primalApiClient: PrimalApiClient): FeedApi = FeedApiImpl(primalApiClient)

    fun createUploadsApi(primalApiClient: PrimalApiClient): UploadApi = throw NotImplementedError()

    fun createImportApi(primalApiClient: PrimalApiClient): PrimalImportApi = PrimalImportApiImpl(primalApiClient)

}
