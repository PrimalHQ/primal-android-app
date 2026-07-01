package net.primal.android.wallet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.core.lightning.LightningAddressChecker
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.parser.WalletTextParser
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.parser.factory.ParserFactory
import net.primal.wallet.data.zaps.NostrZapperFactoryProvider

@Module
@InstallIn(SingletonComponent::class)
object WalletUtilitiesModule {

    @Provides
    @Singleton
    fun providesWalletTextParser(walletRepository: WalletRepository): WalletTextParser =
        ParserFactory.createWalletTextParser(walletRepository = walletRepository)

    @Provides
    @Singleton
    fun providesLightningAddressChecker(dispatcherProvider: DispatcherProvider): LightningAddressChecker =
        LightningAddressChecker(
            dispatcherProvider = dispatcherProvider,
        )

    @Provides
    @Singleton
    fun providesNostrZapperFactory(
        walletRepository: WalletRepository,
        eventRepository: EventRepository,
    ): NostrZapperFactory =
        NostrZapperFactoryProvider.createNostrZapperFactory(
            walletRepository = walletRepository,
            eventRepository = eventRepository,
        )
}
