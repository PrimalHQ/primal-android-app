package net.primal.data.remote.di

import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.FeedApiImpl
import net.primal.data.remote.api.import.PrimalImportApi
import net.primal.data.remote.api.import.PrimalImportApiImpl
import net.primal.data.remote.api.upload.UploadApi
import net.primal.data.remote.uploader.UploadApiSingleConnection
import net.primal.networking.di.PrimalCacheApiClient
import net.primal.networking.di.PrimalUploadApiClient
import org.koin.dsl.module

internal val remoteApiModule = module {
    factory<FeedApi> {
        FeedApiImpl(
            primalApiClient = get(PrimalCacheApiClient)
        )
    }

    factory<UploadApi> {
        UploadApiSingleConnection(
            primalUploadClient = get(PrimalUploadApiClient)
        )
    }

    factory<PrimalImportApi> {
        PrimalImportApiImpl(
            primalApiClient = get(PrimalCacheApiClient),
        )
    }
}
