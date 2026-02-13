package net.primal.domain.connections.nostr

interface NwcService {
    fun initialize(userId: String, onIdleTimeout: (() -> Unit)? = null)

    fun destroy()
}
