package net.primal.android.nostr.repository

import net.primal.android.nostr.api.ZapsApi
import timber.log.Timber
import javax.inject.Inject

class ZapRepository @Inject constructor(
    private val zapsApi: ZapsApi
) {
    suspend fun zap(lightningAddress: String) {
        val result = zapsApi.fetchPayRequest(lightningAddress)
        Timber.d("ZAP PAY REQUEST: ${result?.callback}")
    }
}