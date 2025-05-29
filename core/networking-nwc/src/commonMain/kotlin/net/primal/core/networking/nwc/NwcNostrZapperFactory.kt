package net.primal.core.networking.nwc

import net.primal.core.utils.coroutines.DispatcherProviderFactory

object NwcNostrZapperFactory {
    fun create(nwcData: NostrWalletConnect): NwcNostrZapper {
        return NwcNostrZapper(
            nwcData = nwcData,
            nwcZapHelper = NwcZapHelper(
                dispatcherProvider = DispatcherProviderFactory.create(),
                httpClient = NwcNetworking.httpClient,
            ),
        )
    }
}
