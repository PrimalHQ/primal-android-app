package net.primal.android.nostr.repository

import net.primal.android.networking.relays.NostrWalletRelayPool
import net.primal.android.nostr.api.ZapsApi
import net.primal.android.nostr.model.zap.ZapTarget
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.Relay
import javax.inject.Inject

class ZapRepository @Inject constructor(
    private val zapsApi: ZapsApi,
    private val notary: NostrNotary,
    private val activeAccountStore: ActiveAccountStore,
    private val nostrWalletRelayPool: NostrWalletRelayPool
) {
    suspend fun zap(
        comment: String = "",
        amount: Int = 42,
        target: ZapTarget,
        relays: List<Relay>
    ) {
        val wallet = activeAccountStore.activeUserAccount().nostrWallet ?: throw RuntimeException()

        val lightningAddress = when (target) {
            is ZapTarget.Note -> target.authorLightningAddress
            is ZapTarget.Profile -> target.lightningAddress
        }

        val zapEvent = notary.signZapRequestNostrEvent(
            activeAccountStore.activeUserId(),
            comment = comment,
            target = target,
            relays = relays
        )

        val request = zapsApi.fetchPayRequest(lightningAddress) ?: throw RuntimeException()
        val invoice = zapsApi.fetchInvoice(
            request,
            zapEvent,
            satoshiAmount = amount * 1000,
            comment = comment
        ) ?: throw RuntimeException()

        val walletPayRequest = zapsApi.createWalletPayRequest(invoice)
        val walletPayNostrEvent =
            notary.signWalletInvoiceRequestNostrEvent(walletPayRequest, wallet)

        nostrWalletRelayPool.publishEvent(walletPayNostrEvent)
    }
}