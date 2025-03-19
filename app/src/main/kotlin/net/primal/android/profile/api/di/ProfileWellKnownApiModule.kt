package net.primal.android.profile.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.profile.api.ProfileWellKnownApi
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
class ProfileWellKnownApiModule {

    @Provides
    fun provideProfileWellKnownApi(retrofit: Retrofit): ProfileWellKnownApi = retrofit.create()
}
