package net.primal.wallet.data.service.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.domain.events.EventRepository
import net.primal.domain.lightning.LightningRepository
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.wallet.data.remote.factory.WalletApiServiceFactory
import net.primal.wallet.data.service.concrete.NostrWalletServiceImpl
import net.primal.wallet.data.service.concrete.PrimalWalletServiceImpl

internal object WalletServiceFactory {

    fun createPrimalWalletService(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ) = PrimalWalletServiceImpl(
        primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
            primalApiClient = primalWalletApiClient,
            nostrEventSignatureHandler = nostrEventSignatureHandler,
        ),
    )

    fun createNostrWalletService(lightningRepository: LightningRepository, eventRepository: EventRepository) =
        NostrWalletServiceImpl(
            lightningRepository = lightningRepository,
            eventRepository = eventRepository,
        )
}
