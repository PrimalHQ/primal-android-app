package net.primal.android.wallet.di

import javax.inject.Inject
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.NwcService
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.repository.factory.WalletRepositoryFactory

class NwcServiceFactory @Inject constructor(
    private val walletRepository: WalletRepository,
    private val nostrEncryptionService: NostrEncryptionService,
    private val nwcRepository: NwcRepository,
) {
    fun create(): NwcService =
        WalletRepositoryFactory.createNwcService(
            walletRepository = walletRepository,
            nostrEncryptionService = nostrEncryptionService,
            nwcRepository = nwcRepository,
        )
}
