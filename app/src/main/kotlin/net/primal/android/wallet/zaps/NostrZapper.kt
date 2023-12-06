package net.primal.android.wallet.zaps

interface NostrZapper {
    suspend fun zap(data: ZapRequestData)
}
