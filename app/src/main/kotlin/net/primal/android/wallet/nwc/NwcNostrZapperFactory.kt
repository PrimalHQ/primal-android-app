package net.primal.android.wallet.nwc

import dagger.assisted.AssistedFactory
import net.primal.android.user.domain.NostrWalletConnect

@AssistedFactory
interface NwcNostrZapperFactory {
    fun create(nwcData: NostrWalletConnect): NwcNostrZapper
}
