package net.primal.android.networking.primal.upload.api.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.primal.upload.api.UploadApi
import net.primal.android.networking.primal.upload.api.UploadApiSingleConnection

@Module
@InstallIn(SingletonComponent::class)
interface UploadApiModule {

    @Binds
    fun bindUploadApi(impl: UploadApiSingleConnection): UploadApi
}
