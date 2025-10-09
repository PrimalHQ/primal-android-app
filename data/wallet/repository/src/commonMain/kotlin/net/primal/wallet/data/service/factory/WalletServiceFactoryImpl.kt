package net.primal.wallet.data.service.factory

import net.primal.core.lightning.LightningPayHelper
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.wallet.Wallet
import net.primal.tsunami.TsunamiWalletSdk
import net.primal.wallet.data.remote.factory.WalletApiServiceFactory
import net.primal.wallet.data.service.NostrWalletServiceImpl
import net.primal.wallet.data.service.PrimalWalletServiceImpl
import net.primal.wallet.data.service.TsunamiWalletServiceImpl
import net.primal.wallet.data.service.WalletService

internal class WalletServiceFactoryImpl(
    private val primalWalletService: WalletService<Wallet.Primal>,
    private val nostrWalletService: WalletService<Wallet.NWC>,
    private val tsunamiWalletService: WalletService<Wallet.Tsunami>,
) : WalletServiceFactory {

    override fun getServiceForWallet(wallet: Wallet): WalletService<Wallet> {
        return when (wallet) {
            is Wallet.Primal -> primalWalletService
            is Wallet.NWC -> nostrWalletService
            is Wallet.Tsunami -> tsunamiWalletService
        } as WalletService<Wallet>
    }

    companion object {
        fun createPrimalWalletService(
            primalWalletApiClient: PrimalApiClient,
            nostrEventSignatureHandler: NostrEventSignatureHandler,
        ) = PrimalWalletServiceImpl(
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
        )

        fun createNostrWalletService(eventRepository: EventRepository, lightningPayHelper: LightningPayHelper) =
            NostrWalletServiceImpl(
                eventRepository = eventRepository,
                lightningPayHelper = lightningPayHelper,
            )

        fun createTsunamiWalletService(tsunamiWalletSdk: TsunamiWalletSdk, lightningPayHelper: LightningPayHelper) =
            TsunamiWalletServiceImpl(
                tsunamiWalletSdk = tsunamiWalletSdk,
                lightningPayHelper = lightningPayHelper,
            )
    }
}
