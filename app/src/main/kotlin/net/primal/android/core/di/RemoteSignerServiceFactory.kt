package net.primal.android.core.di

import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.repository.service.factory.AccountServiceFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.NostrKeyPair

class RemoteSignerServiceFactory(
    private val eventSignatureHandler: NostrEventSignatureHandler,
    private val nostrEncryptionHandler: NostrEncryptionHandler,
    private val connectionRepository: ConnectionRepository,
    private val dispatcherProvider: DispatcherProvider,
) {
    private val instances: MutableMap<NostrKeyPair, RemoteSignerService> = mutableMapOf()

    fun create(signerKeyPair: NostrKeyPair): RemoteSignerService =
        instances[signerKeyPair] ?: AccountServiceFactory.createRemoteSignerService(
            signerKeyPair = signerKeyPair,
            eventSignatureHandler = eventSignatureHandler,
            nostrEncryptionHandler = nostrEncryptionHandler,
            connectionRepository = connectionRepository,
            dispatcherProvider = dispatcherProvider,
        ).also { instances.put(signerKeyPair, it) }
}
