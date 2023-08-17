package net.primal.android.nostr.repository

import net.primal.android.nostr.api.ZapsApi
import net.primal.android.nostr.model.zap.ZapTarget
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.Relay
import timber.log.Timber
import javax.inject.Inject

class ZapRepository @Inject constructor(
    private val zapsApi: ZapsApi,
    private val notary: NostrNotary,
    private val activeAccountStore: ActiveAccountStore
) {
    suspend fun zap(
        comment: String = "",
        amount: Int = 42,
        target: ZapTarget,
        relays: List<Relay>
    ) {
        val lightningAddress = when (target) {
            is ZapTarget.Note -> target.authorLightningUrl
            is ZapTarget.Profile -> target.lightningUrl
        }

        val zapEvent = notary.signZapRequestNostrEvent(
            activeAccountStore.activeUserId(),
            comment = comment,
            target = target,
            relays = relays
        )

        val request = zapsApi.fetchPayRequest(lightningAddress)
        Timber.d("ZAP PAY REQUEST: ${request?.callback}")

        if (request == null) {
            throw RuntimeException()
        }

        val invoice = zapsApi.fetchInvoice(request, zapEvent, satoshiAmount = amount * 1000, comment = comment)
        Timber.d("ZAP INVOICE: $invoice")
    }
}