package net.primal.core.networking.nwc

import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.domain.nostr.zaps.NostrZapper

object NwcClientFactory {

    internal val nwcHttpClient by lazy { HttpClientFactory.createHttpClientWithDefaultConfig() }

    private fun create(nwcData: NostrWalletConnect) =
        NwcClientImpl(
            nwcData = nwcData,
            nwcZapHelper = NwcZapHelper(
                dispatcherProvider = DispatcherProviderFactory.create(),
                httpClient = nwcHttpClient,
            ),
        )

    fun createNwcApiClient(nwcData: NostrWalletConnect): NwcApi = create(nwcData)

    fun createNwcNostrZapper(nwcData: NostrWalletConnect): NostrZapper = create(nwcData)
}
