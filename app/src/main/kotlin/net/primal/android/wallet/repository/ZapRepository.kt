package net.primal.android.wallet.repository

import net.primal.android.networking.relays.RelayPool
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.Relay
import net.primal.android.wallet.api.ZapsApi
import net.primal.android.wallet.model.ZapTarget
import java.io.IOException
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
        val zapPayRequest = try {
            zapsApi.fetchZapPayRequest(lightningAddress)
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        } catch (error: IllegalArgumentException) {
            throw MalformedLightningAddressException(cause = error)
        }

        val invoice = try {
            zapsApi.fetchInvoice(
                request = zapPayRequest,
                zapEvent = zapEvent,
                satoshiAmount = amount * 1000,
                comment = comment
            )
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        }

        val walletPayNostrEvent = notary.signWalletInvoiceRequestNostrEvent(
            request = invoice.toWalletPayRequest(),
            nostrWallet
        )

        relayPool.publishEventTo(
            url = nostrWallet.relayUrl,
            nostrEvent = walletPayNostrEvent
        )
    }

    data class ZapFailureException(override val cause: Throwable) : RuntimeException()

    data class MalformedLightningAddressException(override val cause: Throwable) : IllegalArgumentException()
}
