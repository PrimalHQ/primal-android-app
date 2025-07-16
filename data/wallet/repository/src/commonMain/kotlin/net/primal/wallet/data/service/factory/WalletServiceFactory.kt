package net.primal.wallet.data.service.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.wallet.data.remote.factory.WalletApiServiceFactory
import net.primal.wallet.data.service.NostrWalletServiceImpl
import net.primal.wallet.data.service.PrimalWalletServiceImpl

object WalletServiceFactory {

    fun createPrimalWalletService(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ) = PrimalWalletServiceImpl(
        primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
            primalApiClient = primalWalletApiClient,
            nostrEventSignatureHandler = nostrEventSignatureHandler,
        ),
    )

    fun createNostrWalletService() = NostrWalletServiceImpl()
}
