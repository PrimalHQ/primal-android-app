package net.primal.networking.primal.upload.api.di

import net.primal.networking.di.PrimalUploadApiClient
import net.primal.networking.primal.upload.api.UploadApi
import net.primal.networking.primal.upload.api.UploadApiSingleConnection
import org.koin.dsl.module

internal val primalUploadApiModule = module {
    factory<UploadApi> {
        UploadApiSingleConnection(
            primalUploadClient = get(PrimalUploadApiClient)
        )
    }
}
