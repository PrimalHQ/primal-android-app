package net.primal.core.networking.nwc

import net.primal.core.networking.factory.HttpClientFactory
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.wallet.NostrWalletConnect

object NwcClientFactory {

    internal val nwcHttpClient by lazy { HttpClientFactory.createHttpClientWithDefaultConfig() }

    private fun create(nwcData: NostrWalletConnect, nwcZapHelper: NwcZapHelper?) =
        NwcClientImpl(
            nwcData = nwcData,
            nwcZapHelper = nwcZapHelper,
        )

    fun createNwcApiClient(nwcData: NostrWalletConnect): NwcApi = create(nwcData = nwcData, nwcZapHelper = null)

    fun createNwcNostrZapper(nwcData: NostrWalletConnect, nwcZapHelper: NwcZapHelper): NostrZapper =
        create(nwcData = nwcData, nwcZapHelper = nwcZapHelper)
}
