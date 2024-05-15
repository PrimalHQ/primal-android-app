package net.primal.android.core.files.api.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.core.files.api.UploadApi
import net.primal.android.core.files.api.UploadApiImpl

@Module
@InstallIn(SingletonComponent::class)
interface UploadApiModule {

    @Binds
    fun bindUploadApi(impl: UploadApiImpl): UploadApi
}
