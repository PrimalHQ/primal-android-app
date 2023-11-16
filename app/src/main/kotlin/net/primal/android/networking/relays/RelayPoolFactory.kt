package net.primal.android.networking.relays

import dagger.assisted.AssistedFactory
import net.primal.android.user.domain.Relay

@AssistedFactory
interface RelayPoolFactory {

    fun create(relays: List<Relay>): RelayPool
}
