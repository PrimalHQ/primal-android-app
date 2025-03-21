package net.primal.android.profile.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.data.remote.api.users.UserWellKnownApi
import net.primal.data.remote.factory.PrimalApiServiceFactory

@Module
@InstallIn(SingletonComponent::class)
class ProfileWellKnownApiModule {

    @Provides
    fun provideProfileWellKnownApi(): UserWellKnownApi = PrimalApiServiceFactory.createUserWellKnownApi()
}
