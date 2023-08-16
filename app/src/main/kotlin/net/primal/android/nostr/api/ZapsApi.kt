package net.primal.android.nostr.api

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.zap.LightningPayRequest
import net.primal.android.nostr.model.zap.LightningPayResponse

interface ZapsApi {
    suspend fun fetchPayRequest(lightningUrl: String): LightningPayRequest?
    suspend fun fetchInvoice(request: LightningPayRequest, zapEvent: NostrEvent, satoshiAmount: ULong, comment: String): LightningPayResponse?
}