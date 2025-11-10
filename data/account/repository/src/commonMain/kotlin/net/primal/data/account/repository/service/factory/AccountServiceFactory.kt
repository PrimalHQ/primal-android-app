package net.primal.data.account.repository.service.factory

import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.repository.handler.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.factory.AccountManagerFactory
import net.primal.data.account.repository.repository.factory.RepositoryFactory
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
        repositoryFactory: RepositoryFactory,
    ): RemoteSignerService =
        RemoteSignerServiceImpl(
            signerKeyPair = signerKeyPair,
            connectionRepository = connectionRepository,
            sessionRepository = sessionRepository,
            nostrRelayManager = AccountManagerFactory.createNostrRelayManager(
                dispatcherProvider = createDispatcherProvider(),
                signerKeyPair = signerKeyPair,
            ),
            remoteSignerMethodResponseBuilder = RemoteSignerMethodResponseBuilder(
                nostrEventSignatureHandler = eventSignatureHandler,
                nostrEncryptionHandler = nostrEncryptionHandler,
                connectionRepository = connectionRepository,
            ),
            sessionEventProcessor = repositoryFactory.createSessionEventProcessor(),
        )

    fun createNostrEncryptionService(): NostrEncryptionService = NostrEncryptionServiceImpl()
}
