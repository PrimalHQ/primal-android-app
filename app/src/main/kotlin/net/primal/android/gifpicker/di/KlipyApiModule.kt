package net.primal.android.gifpicker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.BuildConfig
import net.primal.data.remote.api.klipy.KlipyApi
import net.primal.data.remote.factory.PrimalApiServiceFactory

@Module
@InstallIn(SingletonComponent::class)
object KlipyApiModule {

    @Provides
    fun provideKlipyApi(): KlipyApi = PrimalApiServiceFactory.createKlipyApi(apiKey = BuildConfig.KLIPY_API_KEY)
}
