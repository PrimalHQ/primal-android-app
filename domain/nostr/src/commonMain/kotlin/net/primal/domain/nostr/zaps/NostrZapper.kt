package net.primal.domain.nostr.zaps

interface NostrZapper {
    suspend fun zap(walletId: String, data: ZapRequestData): ZapResult
}
