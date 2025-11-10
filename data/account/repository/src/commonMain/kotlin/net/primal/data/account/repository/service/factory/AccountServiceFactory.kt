package net.primal.data.account.repository.service.factory

import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.repository.handler.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.repository.InternalSessionEventRepositoryImpl
import net.primal.data.account.repository.repository.factory.provideAccountDatabase
import net.primal.data.account.repository.service.NostrEncryptionServiceImpl
import net.primal.data.account.repository.service.RemoteSignerServiceImpl
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.NostrEncryptionService
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.NostrKeyPair

object AccountServiceFactory {
    fun createRemoteSignerService(
        signerKeyPair: NostrKeyPair,
        eventSignatureHandler: NostrEventSignatureHandler,
        nostrEncryptionHandler: NostrEncryptionHandler,
        connectionRepository: ConnectionRepository,
        sessionRepository: SessionRepository,
    ): RemoteSignerService =
        RemoteSignerServiceImpl(
            signerKeyPair = signerKeyPair,
            connectionRepository = connectionRepository,
            sessionRepository = sessionRepository,
            nostrRelayManager = NostrRelayManager(
                dispatcherProvider = createDispatcherProvider(),
                signerKeyPair = signerKeyPair,
            ),
            remoteSignerMethodResponseBuilder = RemoteSignerMethodResponseBuilder(
                nostrEventSignatureHandler = eventSignatureHandler,
                nostrEncryptionHandler = nostrEncryptionHandler,
                connectionRepository = connectionRepository,
            ),
            internalSessionEventRepository = InternalSessionEventRepositoryImpl(
                dispatchers = createDispatcherProvider(),
                accountDatabase = provideAccountDatabase(),
            ),
        )

    fun createNostrEncryptionService(): NostrEncryptionService = NostrEncryptionServiceImpl()
}
