package net.primal.core.networking.nwc

import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.wallet.NostrWalletConnect

object NwcClientFactory {

    internal val nwcHttpClient by lazy { HttpClientFactory.createHttpClientWithDefaultConfig() }

    private fun create(nwcData: NostrWalletConnect) =
        NwcClientImpl(
            nwcData = nwcData,
            nwcZapHelper = NwcZapHelper(
                dispatcherProvider = createDispatcherProvider(),
                httpClient = nwcHttpClient,
            ),
        )

    fun createNwcApiClient(nwcData: NostrWalletConnect): NwcApi = create(nwcData)

    fun createNwcNostrZapper(nwcData: NostrWalletConnect): NostrZapper = create(nwcData)
}
