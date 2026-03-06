package net.primal.android.namecoin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.namecoin.electrumx.ElectrumxClient
import net.primal.android.namecoin.electrumx.NamecoinNameResolver

@Module
@InstallIn(SingletonComponent::class)
object NamecoinModule {

    @Provides
    @Singleton
    fun provideElectrumxClient(): ElectrumxClient = ElectrumxClient()

    @Provides
    @Singleton
    fun provideNamecoinNameResolver(
        electrumxClient: ElectrumxClient,
    ): NamecoinNameResolver = NamecoinNameResolver(electrumxClient = electrumxClient)
}
