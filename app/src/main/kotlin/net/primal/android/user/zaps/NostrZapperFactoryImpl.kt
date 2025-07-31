package net.primal.android.user.zaps

import javax.inject.Inject
import net.primal.android.settings.wallet.utils.isActivePrimalWallet
import net.primal.android.wallet.repository.WalletNostrZapper
import net.primal.core.networking.nwc.NwcClientFactory
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.service.mappers.asNO

class NostrZapperFactoryImpl @Inject constructor(
    private val primalWalletZapper: WalletNostrZapper,
    private val walletRepository: WalletRepository,
) : NostrZapperFactory {

    override suspend fun createOrNull(walletId: String): NostrZapper? {
        val wallet = walletRepository.getWalletById(walletId = walletId).getOrNull() ?: return null

        return when (wallet) {
            is Wallet.NWC -> wallet.createNwcZapper()
            is Wallet.Primal -> wallet.createPrimalWalletZapper()
        }
    }

    private fun Wallet.Primal.createPrimalWalletZapper(): WalletNostrZapper? {
        return if (isActivePrimalWallet()) {
            primalWalletZapper
        } else {
            null
        }
    }

    private fun Wallet.NWC.createNwcZapper(): NostrZapper? {
        return NwcClientFactory.createNwcNostrZapper(
            nwcData = NostrWalletConnect(
                lightningAddress = this.lightningAddress,
                relays = this.relays,
                pubkey = this.pubkey,
                keypair = this.keypair.asNO(),
            ),
        )
    }
}
