package net.primal.data.account.repository.service.factory

import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.remote.method.processor.RemoteSignerMethodProcessor
import net.primal.data.account.repository.builder.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.repository.InternalSessionEventRepository
import net.primal.data.account.repository.repository.factory.provideAccountDatabase
import net.primal.data.account.repository.service.RemoteSignerServiceImpl
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.NostrKeyPair

object AccountServiceFactory {
    fun createRemoteSignerService(
        signerKeyPair: NostrKeyPair,
        eventSignatureHandler: NostrEventSignatureHandler,
        nostrEncryptionService: NostrEncryptionService,
        nostrEncryptionHandler: NostrEncryptionHandler,
        connectionRepository: ConnectionRepository,
        sessionRepository: SessionRepository,
        sessionInactivityTimeoutInMinutes: Long = 0,
    ): RemoteSignerService =
        RemoteSignerServiceImpl(
            signerKeyPair = signerKeyPair,
            sessionInactivityTimeoutInMinutes = sessionInactivityTimeoutInMinutes,
            connectionRepository = connectionRepository,
            sessionRepository = sessionRepository,
            nostrRelayManager = NostrRelayManager(
                dispatcherProvider = createDispatcherProvider(),
                signerKeyPair = signerKeyPair,
                nostrEncryptionService = nostrEncryptionService,
            ),
            remoteSignerMethodResponseBuilder = RemoteSignerMethodResponseBuilder(
                nostrEventSignatureHandler = eventSignatureHandler,
                nostrEncryptionHandler = nostrEncryptionHandler,
                connectionRepository = connectionRepository,
            ),
            internalSessionEventRepository = InternalSessionEventRepository(
                dispatchers = createDispatcherProvider(),
                accountDatabase = provideAccountDatabase(),
            ),
            methodProcessor = RemoteSignerMethodProcessor(
                nostrEncryptionService = nostrEncryptionService,
            ),
        )
}
