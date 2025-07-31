package net.primal.domain.nostr.zaps

interface NostrZapperFactory {
    suspend fun createOrNull(walletId: String): NostrZapper?
}
