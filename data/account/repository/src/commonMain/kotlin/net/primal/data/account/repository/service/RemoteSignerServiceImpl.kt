package net.primal.data.account.repository.service

import net.primal.domain.account.repository.AccountRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

class RemoteSignerServiceImpl(
    private val eventSignatureHandler: NostrEventSignatureHandler,
    private val accountRepository: AccountRepository,
) : RemoteSignerService {

    init {

        // TODO Observe all unique relays from connections
    }


    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

}
