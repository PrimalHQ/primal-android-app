package net.primal.core.networking.nwc

import net.primal.core.networking.factory.HttpClientFactory
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.wallet.NostrWalletConnect

object NwcClientFactory {

    internal val nwcHttpClient by lazy { HttpClientFactory.createHttpClientWithDefaultConfig() }

    private fun create(nwcData: NostrWalletConnect, lightningPayHelper: LightningPayHelper?) =
        NwcClientImpl(
            nwcData = nwcData,
            lightningPayHelper = lightningPayHelper,
        )

    fun createNwcApiClient(nwcData: NostrWalletConnect): NwcApi = create(nwcData = nwcData, lightningPayHelper = null)

    fun createNwcNostrZapper(nwcData: NostrWalletConnect, lightningPayHelper: LightningPayHelper): NostrZapper =
        create(nwcData = nwcData, lightningPayHelper = lightningPayHelper)
}
