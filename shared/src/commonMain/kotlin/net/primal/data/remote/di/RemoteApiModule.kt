package net.primal.data.remote.di

import net.primal.core.networking.factory.PrimalApiClientFactory
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.FeedApiImpl
import net.primal.data.remote.api.import.PrimalImportApi
import net.primal.data.remote.api.import.PrimalImportApiImpl
import net.primal.data.remote.api.upload.UploadApi
import net.primal.data.remote.uploader.UploadApiSingleConnection
import net.primal.domain.PrimalServerType
import org.koin.dsl.module

internal val remoteApiModule = module {
    factory<FeedApi> {
        FeedApiImpl(
            primalApiClient = PrimalApiClientFactory.create(PrimalServerType.Caching),
        )
    }

    factory<UploadApi> {
        UploadApiSingleConnection(
            primalUploadClient = PrimalApiClientFactory.create(PrimalServerType.Upload),
        )
    }

    factory<PrimalImportApi> {
        PrimalImportApiImpl(
            primalApiClient = PrimalApiClientFactory.create(PrimalServerType.Caching),
        )
    }
}
