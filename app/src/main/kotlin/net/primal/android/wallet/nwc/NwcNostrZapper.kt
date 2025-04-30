package net.primal.android.wallet.nwc

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.wallet.nwc.api.NwcApi
import net.primal.android.wallet.nwc.model.LightningPayRequest
import net.primal.android.wallet.nwc.model.LightningPayResponse
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.ZapFailureException
import net.primal.domain.nostr.zaps.ZapRequestData

class NwcNostrZapper @AssistedInject constructor(
    @Assisted private val nwcData: NostrWalletConnect,
    private val nwcApi: NwcApi,
    private val nostrPublisher: NostrPublisher,
) : NostrZapper {

    override suspend fun zap(data: ZapRequestData) {
        val zapPayRequest = nwcApi.fetchZapPayRequestOrThrow(data.lnUrlDecoded)

        val invoice = nwcApi.fetchInvoiceOrThrow(
            zapPayRequest = zapPayRequest,
            zapEvent = data.userZapRequestEvent,
            satoshiAmountInMilliSats = data.zapAmountInSats * 1000.toULong(),
            comment = data.zapComment,
        )

        try {
            nostrPublisher.publishWalletRequest(userId = data.zapperUserId, invoice = invoice, nwcData = nwcData)
        } catch (error: NostrPublishException) {
            throw ZapFailureException(cause = error)
        } catch (error: SignatureException) {
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
        val fetchInvoiceResult = runCatching {
            this.fetchInvoice(
                request = zapPayRequest,
                zapEvent = zapEvent,
                satoshiAmountInMilliSats = satoshiAmountInMilliSats,
                comment = comment,
            )
        }

        return fetchInvoiceResult.getOrNull()
            ?: throw ZapFailureException(cause = fetchInvoiceResult.exceptionOrNull())
    }
}
