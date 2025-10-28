package net.primal.data.account.repository.manager.factory

import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.domain.nostr.cryptography.NostrKeyPair

internal object AccountManagerFactory {
    var nostrRelayManagerInstance: NostrRelayManager? = null

    fun createNostrRelayManager(
        dispatcherProvider: DispatcherProvider,
        signerKeyPair: NostrKeyPair,
    ): NostrRelayManager =
        nostrRelayManagerInstance ?: NostrRelayManager(
            dispatcherProvider = dispatcherProvider,
            signerKeyPair = signerKeyPair,
        ).also { nostrRelayManagerInstance = it }
}
