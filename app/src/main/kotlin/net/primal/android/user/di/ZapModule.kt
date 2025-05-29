package net.primal.android.user.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.user.zaps.NostrZapperFactoryImpl
import net.primal.core.networking.nwc.LightningAddressChecker
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.zaps.NostrZapperFactory

@Module
@InstallIn(SingletonComponent::class)
class ZapModule {

    @Provides
    fun provideLightningAddressChecker(dispatcherProvider: DispatcherProvider): LightningAddressChecker {
        return LightningAddressChecker(
            dispatcherProvider = dispatcherProvider,
        )
    }

    @Provides
    fun bindNostrZapperFactory(impl: NostrZapperFactoryImpl): NostrZapperFactory = impl
}
