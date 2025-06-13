package net.primal.wallet.data.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.api.PrimalWalletApiImpl
import net.primal.wallet.data.remote.api.PrimalWalletNwcApi
import net.primal.wallet.data.remote.api.PrimalWalletNwcApiImpl

object WalletApiServiceFactory {

    fun createPrimalWalletApi(
        primalApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): PrimalWalletApi =
        PrimalWalletApiImpl(
            primalApiClient = primalApiClient,
            signatureHandler = nostrEventSignatureHandler,
        )

    fun createPrimalWalletNwcApi(
        primalApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): PrimalWalletNwcApi =
        PrimalWalletNwcApiImpl(
            primalApiClient = primalApiClient,
            signatureHandler = nostrEventSignatureHandler,
        )
}
