package net.primal.wallet.data.zaps

import net.primal.core.networking.nwc.NwcClientFactory
import net.primal.core.networking.nwc.NwcZapHelper
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.utils.isActivePrimalWallet
import net.primal.domain.wallet.NostrWalletConnect
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.remote.api.PrimalWalletApi

internal class NostrZapperFactoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletRepository: WalletRepository,
    private val primalWalletApi: PrimalWalletApi,
    private val nwcZapHelper: NwcZapHelper,
) : NostrZapperFactory {

    override suspend fun createOrNull(walletId: String): NostrZapper? {
        val wallet = walletRepository.getWalletById(walletId = walletId).getOrNull() ?: return null

        return when (wallet) {
            is Wallet.Primal -> wallet.createPrimalWalletNostrZapper()
            is Wallet.NWC -> wallet.createNwcNostrZapper()
            is Wallet.Tsunami -> createTsunamiNostrZapper()
        }
    }

    private fun Wallet.Primal.createPrimalWalletNostrZapper(): NostrZapper? {
        return if (isActivePrimalWallet()) {
            PrimalWalletNostrZapper(
                dispatcherProvider = dispatcherProvider,
                primalWalletApi = primalWalletApi,
            )
        } else {
            null
        }
    }

    private fun Wallet.NWC.createNwcNostrZapper(): NostrZapper? {
        return NwcClientFactory.createNwcNostrZapper(
            nwcData = NostrWalletConnect(
                lightningAddress = this.lightningAddress,
                relays = this.relays,
                pubkey = this.pubkey,
                keypair = this.keypair,
            ),
            nwcZapHelper = nwcZapHelper,
        )
    }

    private fun createTsunamiNostrZapper(): NostrZapper? {
        return TsunamiWalletNostrZapper(
            nwcZapHelper = nwcZapHelper,
            walletRepository = walletRepository,
        )
    }
}
