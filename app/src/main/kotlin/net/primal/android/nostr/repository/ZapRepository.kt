package net.primal.android.nostr.repository

import net.primal.android.networking.relays.RelayPool
import net.primal.android.nostr.api.ZapsApi
import net.primal.android.nostr.model.zap.ZapTarget
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.Relay
import javax.inject.Inject

class ZapRepository @Inject constructor(
    private val zapsApi: ZapsApi,
    private val notary: NostrNotary,
    private val relayPool: RelayPool
) {
    suspend fun zap(
        userId: String,
        comment: String = "",
        amount: Int = 42,
        target: ZapTarget,
        relays: List<Relay>,
        nostrWallet: NostrWallet,
    ) {
        val lightningAddress = when (target) {
            is ZapTarget.Note -> target.authorLightningAddress
            is ZapTarget.Profile -> target.lightningAddress
        }

        val zapEvent = notary.signZapRequestNostrEvent(
            userId = userId,
            comment = comment,
            target = target,
            relays = relays
        )

        val zapPayRequest = zapsApi.fetchZapPayRequest(lightningAddress) ?: throw ZapFailureException()
        val invoice = zapsApi.fetchInvoice(
            request = zapPayRequest,
            zapEvent = zapEvent,
            satoshiAmount = amount * 1000,
            comment = comment
        ) ?: throw ZapFailureException()

        val walletPayRequest = zapsApi.createWalletPayRequest(invoice)
        val walletPayNostrEvent = notary.signWalletInvoiceRequestNostrEvent(walletPayRequest, nostrWallet)

        relayPool.publishEventTo(
            url = nostrWallet.relayUrl,
            nostrEvent = walletPayNostrEvent
        )
    }


    class ZapFailureException : RuntimeException()
}