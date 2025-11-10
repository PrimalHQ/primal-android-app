package net.primal.android.core.di

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.data.account.repository.repository.factory.RepositoryFactory
import net.primal.data.account.repository.service.factory.AccountServiceFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.NostrKeyPair

@Singleton
class RemoteSignerServiceFactory @Inject constructor(
    private val eventSignatureHandler: NostrEventSignatureHandler,
    private val nostrEncryptionHandler: NostrEncryptionHandler,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val repositoryFactory: RepositoryFactory,
) {
    fun create(signerKeyPair: NostrKeyPair): RemoteSignerService =
        AccountServiceFactory.createRemoteSignerService(
            signerKeyPair = signerKeyPair,
            eventSignatureHandler = eventSignatureHandler,
            nostrEncryptionHandler = nostrEncryptionHandler,
            connectionRepository = connectionRepository,
            sessionRepository = sessionRepository,
            repositoryFactory = repositoryFactory,
        )
}
