package net.primal.core.nips.encryption.service.factory

import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.nips.encryption.service.NostrEncryptionServiceImpl

object NipsEncryptionFactory {
    fun createNostrEncryptionService(): NostrEncryptionService = NostrEncryptionServiceImpl()
}
