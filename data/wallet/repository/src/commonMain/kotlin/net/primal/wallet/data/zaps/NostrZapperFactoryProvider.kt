package net.primal.wallet.data.zaps

import net.primal.core.networking.nwc.NwcZapHelper
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.remote.factory.WalletApiServiceFactory

object NostrZapperFactoryProvider {

    private val dispatcherProvider = createDispatcherProvider()

    fun createNostrZapperFactory(
        walletRepository: WalletRepository,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
        primalWalletApiClient: PrimalApiClient,
    ): NostrZapperFactory {
        return NostrZapperFactoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletRepository = walletRepository,
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
            nwcZapHelper = NwcZapHelper(
                dispatcherProvider = dispatcherProvider,
            ),
        )
    }
}
