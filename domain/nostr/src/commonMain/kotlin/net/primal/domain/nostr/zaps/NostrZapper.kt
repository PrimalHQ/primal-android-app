package net.primal.domain.nostr.zaps

interface NostrZapper {
    suspend fun zap(data: ZapRequestData): ZapResult
}
