package net.primal.android.core.di

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.data.account.repository.repository.factory.AccountRepositoryFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.nostr.cryptography.NostrKeyPair

@Singleton
class SignerConnectionInitializerFactory @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val nostrEncryptionService: NostrEncryptionService,
) {
    fun create(signerKeyPair: NostrKeyPair) =
        AccountRepositoryFactory.createSignerConnectionInitializer(
            signerKeyPair = signerKeyPair,
            connectionRepository = connectionRepository,
            nostrEncryptionService = nostrEncryptionService,
        )
}
