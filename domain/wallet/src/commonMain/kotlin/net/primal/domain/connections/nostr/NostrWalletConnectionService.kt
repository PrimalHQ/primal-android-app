package net.primal.domain.connections.nostr

interface NostrWalletConnectionService {
    fun initialize(userId: String)

    fun destroy()
}
