package net.primal.data.account.repository.repository.factory

import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.manager.factory.AccountManagerFactory
import net.primal.data.account.repository.repository.ConnectionRepositoryImpl
import net.primal.data.account.repository.repository.SignerConnectionInitializer
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.nostr.cryptography.NostrKeyPair

abstract class RepositoryFactory {
    private val dispatcherProvider = createDispatcherProvider()

    abstract fun resolveAccountDatabase(): AccountDatabase

    fun createConnectionRepository(): ConnectionRepository =
        ConnectionRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
        )

    fun createSignerConnectionInitializer(
        signerKeyPair: NostrKeyPair,
        connectionRepository: ConnectionRepository,
    ): SignerConnectionInitializer =
        SignerConnectionInitializer(
            connectionRepository = connectionRepository,
            nostrRelayManager = AccountManagerFactory.createNostrRelayManager(
                dispatcherProvider = dispatcherProvider,
                signerKeyPair = signerKeyPair,
            ),
        )
}
