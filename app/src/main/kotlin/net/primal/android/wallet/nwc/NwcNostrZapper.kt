package net.primal.android.wallet.nwc

import java.io.IOException
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.wallet.nwc.api.NwcApi
import net.primal.android.wallet.nwc.model.LightningPayRequest
import net.primal.android.wallet.nwc.model.LightningPayResponse
import net.primal.android.wallet.zaps.NostrZapper
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapRequestData

class NwcNostrZapper(
    private val relaysManager: RelaysManager,
    private val notary: NostrNotary,
    private val nwcApi: NwcApi,
    private val nostrWallet: NostrWalletConnect,
) : NostrZapper {

    override suspend fun zap(data: ZapRequestData) {
        val zapPayRequest = nwcApi.fetchZapPayRequestOrThrow(data.lnUrlDecoded)

        val invoice = nwcApi.fetchInvoiceOrThrow(
            zapPayRequest = zapPayRequest,
            zapEvent = data.userZapRequestEvent,
            satoshiAmountInMilliSats = data.zapAmountInSats * 1000.toULong(),
            comment = data.zapComment,
        )

        val walletPayNostrEvent = notary.signWalletInvoiceRequestNostrEvent(
            request = invoice.toWalletPayRequest(),
            nwc = nostrWallet,
        )

        try {
            relaysManager.publishWalletEvent(nostrEvent = walletPayNostrEvent)
        } catch (error: NostrPublishException) {
            throw ZapFailureException(cause = error)
        }
    }

    private suspend fun NwcApi.fetchZapPayRequestOrThrow(lnUrl: String): LightningPayRequest {
        return try {
            fetchZapPayRequest(lnUrl)
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        } catch (error: IllegalArgumentException) {
            throw ZapFailureException(cause = error)
        }
    }

    private suspend fun NwcApi.fetchInvoiceOrThrow(
        zapPayRequest: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmountInMilliSats: ULong,
        comment: String = "",
    ): LightningPayResponse {
        return try {
            this.fetchInvoice(
                request = zapPayRequest,
                zapEvent = zapEvent,
                satoshiAmountInMilliSats = satoshiAmountInMilliSats,
                comment = comment,
            )
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        }
    }
}
