package net.primal.wallet.data.zaps

import net.primal.core.lightning.LightningPayHelper
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.wallet.WalletRepository

object NostrZapperFactoryProvider {

    private val dispatcherProvider = createDispatcherProvider()

    fun createNostrZapperFactory(
        walletRepository: WalletRepository,
        eventRepository: EventRepository? = null,
    ): NostrZapperFactory {
        return NostrZapperFactoryImpl(
            walletRepository = walletRepository,
            lightningPayHelper = LightningPayHelper(
                dispatcherProvider = dispatcherProvider,
            ),
            eventRepository = eventRepository,
        )
    }
}
