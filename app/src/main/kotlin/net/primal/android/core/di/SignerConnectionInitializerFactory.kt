package net.primal.android.core.di

import net.primal.data.account.repository.repository.factory.AccountRepositoryFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.nostr.cryptography.NostrKeyPair

class SignerConnectionInitializerFactory(
    private val connectionRepository: ConnectionRepository,
) {
    fun create(signerKeyPair: NostrKeyPair) =
        AccountRepositoryFactory.createSignerConnectionInitializer(
            signerKeyPair = signerKeyPair,
            connectionRepository = connectionRepository,
        )
}
