package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.data.remote.api.lightning.LightningApi
import net.primal.data.remote.factory.PrimalApiServiceFactory

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    fun providesLightningApi(): LightningApi = PrimalApiServiceFactory.createLightningApi()
}
